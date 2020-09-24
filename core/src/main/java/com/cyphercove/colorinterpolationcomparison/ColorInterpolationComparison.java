package com.cyphercove.colorinterpolationcomparison;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cyphercove.gdxtween.graphics.ColorSpace;
import com.cyphercove.gdxtween.graphics.GtColor;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

import static com.badlogic.gdx.math.Interpolation.fade;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ColorInterpolationComparison extends ApplicationAdapter {
    Pixmap whitePixmap;
    Texture white;
    Viewport viewport = new ScreenViewport();
    BasicColorPicker2 firstColorPicker;
    BasicColorPicker2 secondColorPicker;
    Color firstColor = new Color(Color.BLUE);
    Color secondColor = new Color(Color.YELLOW);
    Color tmpColor = new Color();
    Stage stage;
    PlatformResolver platformResolver;
    boolean isDarkBackground = true;

    private static class Item {
        String label;
        ColorSpace colorSpace;
        String toolTip;

        public Item(String label, ColorSpace colorSpace, String toolTip) {
            this.label = label;
            this.colorSpace = colorSpace;
            this.toolTip = toolTip;
        }
    }

    private static final Item[] items = {
            new Item("RGB", ColorSpace.Rgb, "Directly interpolates the gamma-corrected RGB values. aka sRGB."),
            new Item("Linear RGB", ColorSpace.DegammaRgb, "Interpolates in linear RGB space, so gamma " +
                    "correction is removed. The blend is even in terms of light energy, but it does not appear even to the eye."),
            new Item("Lab", ColorSpace.DegammaLab, "Interpolates in CIELAB color space, which was designed " +
                    "for visually even intensity changes. Some color combinations may produce faint extra hues in the middle of the interpolation."),
            new Item("LMS Compressed", ColorSpace.DegammaLmsCompressed, "Interpolates in LMS space after " +
                    "apply a gamma compression to produce extremely visually even blends. This space is and intermediate " +
                    "stage of transforming an RGB value to IPT space."),
            new Item("IPT", ColorSpace.DegammaIpt, "Interpolates in IPT space, which produces extremely " +
                    "visually even blends."),
            new Item("Lch", ColorSpace.DegammaLch, "Interpolates in Lch space, a cylindrical transformation" +
                    " of CIELAB space. Hue is one of the dimensions, so it can produce unrelated intermediate hues."),
            new Item("HSL", ColorSpace.Hsl, "Interpolates in HSL space. This can produce unrelated " +
                    "intermediate hues. Since saturation is independent of brightness in the definition of HSL, bright " +
                    "colors can appear when blending between almost-white colors and gray-ish or dark colors."),
            new Item("HCL", ColorSpace.Hcl, "Interpolates in HSL space using chroma instead of saturation, " +
                    "which avoids the issue of unwanted intermediate bright color when blending between almost-white" +
                    "colors and gray-ish or dark colors."),
            new Item("HSV", ColorSpace.Hsv, "Interpolates in HSV space. This can produce unrelated intermediate hues.")
    };

    public ColorInterpolationComparison() {
        this(null);
    }

    public ColorInterpolationComparison(PlatformResolver platformResolver) {
        this.platformResolver = platformResolver;
    }

    @Override
    public void create () {
        VisUI.load();
        Gdx.graphics.setContinuousRendering(false);
        stage = new Stage(viewport);

        whitePixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        whitePixmap.setColor(Color.WHITE);
        whitePixmap.fill();
        white = new Texture(whitePixmap);

        setupUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void setupUI () {
        VisUI.getSkin().get("default", Label.LabelStyle.class).fontColor.set(Color.GRAY);

        Table table = new Table();
        table.setFillParent(true);
        table.pad(15);

        firstColorPicker = new BasicColorPicker2();
        firstColorPicker.setShowColorPreviews(false);
        firstColorPicker.setColor(firstColor);
        firstColorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                firstColor.set(newColor);
            }
        });
        table.add(firstColorPicker).center().pad(20);

        TooltipManager tooltipManager = new TooltipManager() {
            {
                initialTime = 0.15f;
                resetTime = 0.5f;
                offsetX = -230;
                hideAll();
            }
            @Override
            protected void showAction (Tooltip tooltip) {
                float actionTime = 0.25f;
                Container<?> container = tooltip.getContainer();
                container.setTransform(true);
                container.getColor().a = 0.2f;
                container.setScale(0.05f);
                container.addAction(parallel(fadeIn(actionTime, fade), scaleTo(1, 1, actionTime, fade)));
            }
        };
        VisUI.getSkin().get("default", TextTooltip.TextTooltipStyle.class).wrapWidth = 200;
        VisUI.getSkin().get("default", TextTooltip.TextTooltipStyle.class).label.fontColor = Color.WHITE;
        Table innerTable = new Table(VisUI.getSkin());
        for (Item item : items) {
            Label label = new Label(item.label, VisUI.getSkin());
            label.addListener(new TextTooltip(item.toolTip, tooltipManager, VisUI.getSkin()));
            label.setAlignment(Align.center);
            innerTable.add(label).center().fill();
            innerTable.add(new ColorTransition(item.colorSpace)).growX().height(30).space(10);
            innerTable.row();
        }
        table.add(innerTable).grow();

        secondColorPicker = new BasicColorPicker2();
        secondColorPicker.setShowColorPreviews(false);
        secondColorPicker.setColor(secondColor);
        secondColorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                secondColor.set(newColor);
            }
        });
        table.add(secondColorPicker).center().pad(20);

        table.row().colspan(3);
        final CheckBox checkBox = new CheckBox(" Dark mode", VisUI.getSkin());
        checkBox.setChecked(isDarkBackground);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isDarkBackground = checkBox.isChecked();
                if (platformResolver != null) {
                    platformResolver.setBodyBackgroundColor(Color.rgb888(isDarkBackground ? Color.BLACK : Color.WHITE));
                }
            }
        });
        table.add(checkBox).bottom().left();
        stage.addActor(table);
    }

    private class ColorTransition extends Widget {
        ColorSpace colorSpace;

        public ColorTransition(ColorSpace colorSpace) {
            this.colorSpace = colorSpace;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            int segments = 80;
            float segmentWidth = getWidth() / segments;
            for (int i = 0; i < segments; i++) {
                float progress = (float)i / (segments - 1);
                tmpColor.set(firstColor);
                GtColor.lerp(tmpColor, secondColor, progress, colorSpace, false);
                batch.setColor(tmpColor);
                batch.draw(white, getX() + segmentWidth * i, getY(), segmentWidth, getHeight());
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render () {
        if (isDarkBackground)
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        else
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        whitePixmap.dispose();
        white.dispose();
        firstColorPicker.dispose();
        secondColorPicker.dispose();
    }
}
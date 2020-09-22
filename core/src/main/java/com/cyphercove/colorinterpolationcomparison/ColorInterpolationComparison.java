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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cyphercove.gdxtween.graphics.ColorSpace;
import com.cyphercove.gdxtween.graphics.GtColor;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.color.BasicColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

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

    private static final ObjectMap<String, ColorSpace> choices = new OrderedMap<>();
    static {
        choices.put("RGB", ColorSpace.Rgb);
        choices.put("Linear RGB", ColorSpace.DegammaRgb);
        choices.put("Lab", ColorSpace.DegammaLab);
        choices.put("LMS Compressed", ColorSpace.DegammaLmsCompressed);
        choices.put("IPT", ColorSpace.DegammaIpt);
        choices.put("Lch", ColorSpace.DegammaLch);
        choices.put("HSL", ColorSpace.Hsl);
        choices.put("HCL", ColorSpace.Hcl);
        choices.put("HSV", ColorSpace.Hsv);
    }

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

        Table innerTable = new Table(VisUI.getSkin());
        for (ObjectMap.Entry<String, ColorSpace> entry : choices) {
            innerTable.add(entry.key).center();
            innerTable.add(new ColorTransition(entry.value)).growX().height(30).space(10);
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
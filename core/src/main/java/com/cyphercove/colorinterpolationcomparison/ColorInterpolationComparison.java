package com.cyphercove.colorinterpolationcomparison;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cyphercove.gdxtween.graphics.GtColor;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.color.BasicColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ColorInterpolationComparison extends ApplicationAdapter {
    Pixmap whitePixmap;
    Texture white;
    Viewport viewport = new ScreenViewport();
    BasicColorPicker firstColorPicker;
    BasicColorPicker secondColorPicker;
    Color firstColor = new Color(Color.BLUE);
    Color secondColor = new Color(Color.YELLOW);
    Color tmpColor = new Color();
    Stage stage;

    @Override
    public void create () {
        VisUI.load();
        stage = new Stage(viewport);

        whitePixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        whitePixmap.setColor(Color.WHITE);
        whitePixmap.fill();
        white = new Texture(whitePixmap);

        setupUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void setupUI () {
        Table table = new Table();
        table.setFillParent(true);
        table.pad(15);

        firstColorPicker = new BasicColorPicker();
        firstColorPicker.setColor(firstColor);
        firstColorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                firstColor.set(newColor);
            }
        });
        table.add(firstColorPicker).center().pad(20);

        Table innerTable = new Table(VisUI.getSkin());
        String[] types = { "Rgb", "LinearRgb", "Hsv", "Lab", "Lch" };
        for (int i = 0; i < types.length; i++) {
            innerTable.add(types[i]).center();
            innerTable.add(new ColorTransition(i)).growX().height(50).space(10);
            innerTable.row();
        }
        table.add(innerTable).grow();

        secondColorPicker = new BasicColorPicker();
        secondColorPicker.setColor(secondColor);
        secondColorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                secondColor.set(newColor);
            }
        });
        table.add(secondColorPicker).center().pad(20);
        stage.addActor(table);
    }

    private class ColorTransition extends Widget {
        int spaceType;

        public ColorTransition(int spaceType) {
            this.spaceType = spaceType;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            int segments = 80;
            float segmentWidth = getWidth() / segments;
            for (int i = 0; i < segments; i++) {
                float progress = (float)i / (segments - 1);
                tmpColor.set(firstColor);
                switch (spaceType) {
                    case 0:
                        GtColor.lerpRgb(tmpColor, secondColor, progress);
                        break;
                    case 1:
                        GtColor.lerpLinearRgb(tmpColor, secondColor, progress);
                        break;
                    case 2:
                        GtColor.lerpHsv(tmpColor, secondColor, progress);
                        break;
                    case 3:
                        GtColor.lerpLab(tmpColor, secondColor, progress);
                        break;
                    case 4:
                        GtColor.lerpLch(tmpColor, secondColor, progress);
                        break;
                }
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
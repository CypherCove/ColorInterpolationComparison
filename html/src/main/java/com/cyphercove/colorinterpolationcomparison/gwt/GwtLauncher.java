package com.cyphercove.colorinterpolationcomparison.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.cyphercove.colorinterpolationcomparison.ColorInterpolationComparison;
import com.cyphercove.colorinterpolationcomparison.PlatformResolver;
import org.w3c.dom.css.RGBColor;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Launches the GWT application.
 */
public class GwtLauncher extends GwtApplication implements PlatformResolver {
    ////USE THIS CODE FOR A FIXED SIZE APPLICATION
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(960, 480);
        config.disableAudio = true;
        return config;
    }
    ////END CODE FOR FIXED SIZE APPLICATION

    ////UNCOMMENT THIS CODE FOR A RESIZABLE APPLICATION
    //	PADDING is to avoid scrolling in iframes, set to 20 if you have problems
    //	private static final int PADDING = 0;
    //
    //	@Override
    //	public GwtApplicationConfiguration getConfig() {
    //		int w = Window.getClientWidth() - PADDING;
    //		int h = Window.getClientHeight() - PADDING;
    //		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(w, h);
    //		Window.enableScrolling(false);
    //		Window.setMargin("0");
    //		Window.addResizeHandler(new ResizeListener());
    //		cfg.preferFlash = false;
    //		return cfg;
    //	}
    //
    //	class ResizeListener implements ResizeHandler {
    //		@Override
    //		public void onResize(ResizeEvent event) {
    //          if (Gdx.graphics.isFullscreen()) return;
    //			int width = event.getWidth() - PADDING;
    //			int height = event.getHeight() - PADDING;
    //			getRootPanel().setWidth("" + width + "px");
    //			getRootPanel().setHeight("" + height + "px");
    //			getApplicationListener().resize(width, height);
    //			Gdx.graphics.setWindowedMode(width, height);
    //		}
    //	}
    ////END OF CODE FOR RESIZABLE APPLICATION

    @Override
    public ApplicationListener createApplicationListener() {
        return new ColorInterpolationComparison(this);
    }

    @Override
    public void setBodyBackgroundColor(int color) {
        $("body").css("background-color", toCssColorString(color));
    }

    private static String toCssColorString(int color) {
        String hex = Integer.toHexString(color);
        StringBuilder sb = new StringBuilder("#");
        for (int i = 6 - hex.length(); i > 0; i--) {
            sb.append('0');
        }
        return sb + hex;
    }
}

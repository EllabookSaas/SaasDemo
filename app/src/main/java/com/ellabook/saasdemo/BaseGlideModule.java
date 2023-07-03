package com.ellabook.saasdemo;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * created by dongdaqing 10/9/21 4:52 PM
 */
@GlideModule
public class BaseGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}

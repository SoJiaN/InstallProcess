package com.example.a22257.custominstallprocess.textViewMultiline;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
//import com.bumptech.glide.request.animation.GlideAnimation;
//import com.bumptech.glide.request.target.SimpleTarget;
//import com.worktile.kernel.Kernel;
//import com.worktile.ui.component.imageviewer.ImageViewerActivity;
//import org.commonmark.ext.autolink.AutolinkExtension;

import java.util.ArrayList;

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: Moki
 * Email: mosicou@gmail.com
 * Date: 2018/2/22
 * Time: 17:12
 * Desc:
 */

public class WtParser {
    private ArrayList<String> mImageUrls = new ArrayList<>();
    @Nullable
    private Config mConfig;

    private int mCanvasWidth = 720;

//    public CharSequence parse(String markdown, int canvasWidth, @Nullable Config config) {
//        mConfig = config;
//        mCanvasWidth = canvasWidth;
//        return parse(markdown, config);
//    }

//    public CharSequence parse(String markdown, @Nullable Config config) {
//        mConfig = config;
////        markdown = FormatConverterCommonMark.converter(markdown);这行代码会增加解析时间，造成明显的卡顿
//        int length = markdown.length();
//        int index = 0;
//        while ((index < length) && markdown.charAt(index) == '\t') {
//            index++;
//        }
//        markdown = index > 0 ? markdown.substring(index) : markdown;
//        markdown = markdown.trim();
//        Parser parser = new Parser.Builder()
//                .extensions(Arrays.asList(
//                        StrikethroughExtension.create(),
//                        TablesExtension.create(),
//                        TaskListExtension.create(),
//                        AutolinkExtension.create()
//                ))
//                .inlineParserFactory(WtInlineParserFactory.create())
//                .build();
//        SpannableConfiguration configuration = SpannableConfiguration
//                .builder(Kernel.getInstance().getActivityContext())
//                .theme(new WtSpannableTheme(SpannableTheme.create(Kernel.getInstance().getActivityContext())))
//                .asyncDrawableLoader(new AsyncDrawable.Loader() {
//                    @Override
//                    public void load(@NonNull String destination, @NonNull AsyncDrawable drawable) {
//                        Glide.with(Kernel.getInstance().getActivityContext())
//                                .load(destination)
//                                .into(new SimpleTarget<GlideDrawable>() {
//                                    @Override
//                                    public void onResourceReady(GlideDrawable resource,
//                                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
//                                        if (resource instanceof GlideBitmapDrawable) {
//                                            GlideBitmapDrawable bitmapDrawable = (GlideBitmapDrawable) resource;
//                                            Bitmap resourceBitmap = bitmapDrawable.getBitmap();
//                                            if (mCanvasWidth == 0) return;
//                                            if (mCanvasWidth > 720) mCanvasWidth = 720;
//                                            int resourceWidth = resourceBitmap.getWidth();
//                                            Bitmap scaleBitmap = resourceBitmap;
//                                            if (resourceWidth > mCanvasWidth) {
//                                                float ratio = (float) mCanvasWidth / (float) resourceWidth;
//                                                Matrix matrix = new Matrix();
//                                                matrix.setScale(ratio, ratio);
//                                                scaleBitmap = Bitmap.createBitmap(
//                                                        resourceBitmap,
//                                                        0,
//                                                        0,
//                                                        resourceBitmap.getWidth(),
//                                                        resourceBitmap.getHeight(),
//                                                        matrix,
//                                                        true
//                                                );
//                                            }
//                                            bitmapDrawable = new GlideBitmapDrawable(
//                                                    Kernel.getInstance()
//                                                            .getActivityContext()
//                                                            .getResources(),
//                                                    scaleBitmap
//                                            );
//                                            setAsyncDrawableBounds(bitmapDrawable);
//                                            drawable.setResult(bitmapDrawable);
//                                        } else {
//                                            setAsyncDrawableBounds(resource);
//                                            drawable.setResult(resource);
//                                        }
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void cancel(@NonNull String destination) {
//
//                    }
//                })
//                .build();
//        WtSpannableRenderer renderer = new WtSpannableRenderer();
//        Node node = parser.parse(markdown);
//        CharSequence text = renderer.render(configuration, node);
//
//        if (text instanceof SpannableStringBuilder) {
//            SpannableStringBuilder builder = (SpannableStringBuilder) text;
//            AsyncDrawableSpan[] imageSpans = builder.getSpans(
//                    0,
//                    builder.length(),
//                    AsyncDrawableSpan.class
//            );
//            for (int i = imageSpans.length - 1; i >= 0; i--) {
//                AsyncDrawableSpan imageSpan = imageSpans[i];
//                String imageUrl = imageSpan.getDrawable().getDestination();
//                mImageUrls.add(imageUrl);
//                int start = builder.getSpanStart(imageSpan);
//                int end = builder.getSpanEnd(imageSpan);
//                int finalI = i;
//                builder.setSpan(
//                        new ClickableSpan() {
//                            @Override
//                            public void onClick(View widget) {
//                                ImageViewerActivity.start(
//                                        widget.getContext(),
//                                        mImageUrls,
//                                        mImageUrls,
//                                        imageSpans.length - 1 - finalI
//                                );
//                            }
//                        },
//                        start,
//                        end,
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
//            }
//        }
//
//        return text;
//    }

//    @Deprecated
//    public CharSequence parse(String markdown) {
//        return parse(markdown, null);
//    }

    private void setAsyncDrawableBounds(Drawable resource) {
        if (mCanvasWidth == 0) return;
        int width = resource.getIntrinsicWidth();
        int height = resource.getIntrinsicHeight();
        float ratio = (float) height / (float) width;
        if (width > mCanvasWidth) {
            width = mCanvasWidth;
            height = (int) (width * ratio);
        }
        resource.setBounds(0, 0, width, height);
    }

//    class WtSpannableTheme extends SpannableTheme {
//        WtSpannableTheme(SpannableTheme spannableTheme) {
//            super(SpannableTheme
//                    .builder(spannableTheme)
//                    .linkColor(mConfig != null
//                            ? mConfig.linkColor
//                            : ContextCompat.getColor(Kernel.getInstance().getApplicationContext(), R.color.main_green)));
//        }
//
//        @Override
//        public void applyLinkStyle(@NonNull Paint paint) {
//            super.applyLinkStyle(paint);
//            paint.setUnderlineText(mConfig != null && mConfig.linkUnderline);
//        }
//    }

    public static class Config {
        @ColorInt
        int linkColor;
        boolean linkUnderline;

        public Config setLinkColor(@ColorInt int linkColor) {
            this.linkColor = linkColor;
            return this;
        }

        public Config setLinkUnderline(boolean linkUnderline) {
            this.linkUnderline = linkUnderline;
            return this;
        }

        public Config config() {
            return this;
        }
    }
}


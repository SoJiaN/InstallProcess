package com.example.a22257.custominstallprocess.textViewMultiline;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Canvas;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;

//import com.zzhoujay.markdown.style.LongPressClickableSpan;

import ru.noties.markwon.Markwon;

/**
 * Copyright © 2017 Worktile. All Rights Reserved.
 * Author: Moki
 * Email: mosicou@gmail.com
 * Date: 2017/10/12
 * Time: 15:43
 * Desc:
 */
public class WtTextView extends android.support.v7.widget.AppCompatTextView {
    private int mCanvasWidth;

    public WtTextView(Context context) {
        super(context);
        init();
    }

    public WtTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WtTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CharSequence charSequence = getText();
        if (charSequence instanceof Spannable) {
            Spannable spannable = (Spannable) charSequence;
            ClickableSpan[] clickableSpans = spannable.getSpans(0, spannable.length(), ClickableSpan.class);
            if (clickableSpans.length == 0 && !hasOnClickListeners()) {
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvasWidth = canvas.getWidth();
    }

    public void setMarkdown(String markdown) {
        if (TextUtils.isEmpty(markdown)) return;
//        CharSequence text = new WtParser().parse(markdown, mCanvasWidth, null);

        Markwon.unscheduleDrawables(this);
        Markwon.unscheduleTableRows(this);

//        setText(text);

        Markwon.scheduleDrawables(this);
        Markwon.scheduleTableRows(this);
    }

    @BindingAdapter("markdown")
    public static void setMarkDown(WtTextView textView, CharSequence text) {
        Markwon.unscheduleDrawables(textView);
        Markwon.unscheduleTableRows(textView);

        textView.setText(text);

        Markwon.scheduleDrawables(textView);
        Markwon.scheduleTableRows(textView);
    }

    @BindingAdapter("markdown")
    public static void setMarkDown(WtTextView textView, String markdown) {
        textView.setMarkdown(markdown);
    }

    @BindingAdapter("wtLink")
    public static void setWtLink(WtTextView textView, CharSequence wtLink) {
        textView.setText(wtLink);
    }
}

package com.wansnn.csc.wsbulb.jl_dialog;


import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wansnn.csc.wsbulb.R;
import com.wansnn.csc.wsbulb.jl_dialog.interfaces.OnViewClickListener;


/**
 * Created by chensenhua on 2018/1/15.
 */

public class Jl_Dialog extends DialogFragment {
    private String tag = getClass().getSimpleName();

    private boolean isShow = false;
    private Builder builder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        if (builder == null && savedInstanceState != null && savedInstanceState.getParcelable("builder") != null) {
            builder = (Builder) savedInstanceState.get("builder");
        }
        if (builder != null && builder.containerRes != 0) {
            view = inflater.inflate(builder.containerRes, container, false);
        } else if (builder != null && builder.containerView != null) {
            view = inflater.inflate(R.layout.dialog_container, container, false);
            ViewGroup linearLayout = (ViewGroup) view.findViewById(R.id.ll_dialog_container);
            linearLayout.removeAllViews();
            ViewGroup viewGroup = (ViewGroup) builder.containerView.getParent();
            if (viewGroup != null) {
                viewGroup.removeAllViews();
            }
            linearLayout.addView(builder.containerView);
        } else {
            view = inflater.inflate(R.layout.dialog_container, container, false);
            intiDefaultDialog(view);
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    public Builder getBuilder() {
        return builder;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }

        WindowManager.LayoutParams mLayoutParams = window.getAttributes();
        mLayoutParams.dimAmount = 0.5f;
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;


        mLayoutParams.width = builder.width < 0 ? WindowManager.LayoutParams.WRAP_CONTENT : (int) (builder.width * getScreenWidth());
        mLayoutParams.height = builder.height < 0 ? WindowManager.LayoutParams.WRAP_CONTENT : (int) (builder.height * getScreenHeight());
/*        mLayoutParams.width =  WindowManager.LayoutParams.WRAP_CONTENT ;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT  ;*/

        window.getDecorView().getRootView().setBackgroundColor(Color.TRANSPARENT);
        window.setAttributes(mLayoutParams);
        setCancelable(builder.cancel);
    }


    private int getScreenWidth() {
        if (getContext() == null) return 0;
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        if (getContext() == null) return 0;
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public boolean isShow() {
        return isShow;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        isShow = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();

    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShow = false;
        super.onDismiss(dialog);
    }


    public void intiDefaultDialog(View view) {
        if (builder.backgroundColor != -2) {
            view.findViewById(R.id.ll_dialog_container).setBackgroundColor(builder.backgroundColor);
        }
        initHeader(view);
        initContent(view);
        initBottom(view);
    }

    private void initHeader(View view) {
        if (!TextUtils.isEmpty(builder.title)) {
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(builder.title);
            if (builder.titleColor != -2) {
                tvTitle.setTextColor(builder.titleColor);
            }
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if( getDialog().getWindow() == null) return;
        WindowManager.LayoutParams mLayoutParams = getDialog().getWindow().getAttributes();
        if (builder.animations != 0) {
            mLayoutParams.windowAnimations = builder.animations;
        }
        if (builder.height >= 0) {

        }
    }

    private void initContent(View view) {

        RelativeLayout contentView = null;

        if (builder.contentLayoutView != null || builder.contentLayoutRes != 0) {
            contentView = (RelativeLayout) view.findViewById(R.id.dialog_content_container);
            contentView.setVisibility(View.VISIBLE);
            contentView.removeAllViews();
        }
        if (builder.contentLayoutView != null) {
            ViewGroup viewGroup = (ViewGroup) builder.contentLayoutView.getParent();
            if (viewGroup != null) {
                viewGroup.removeAllViews();
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            contentView.addView(builder.contentLayoutView, params);
        } else if (builder.contentLayoutRes != 0) {
            View child = LayoutInflater.from(getContext()).inflate(builder.contentLayoutRes, contentView, false);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) child.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            view.setLayoutParams(params);
            contentView.addView(child);
        } else if (!TextUtils.isEmpty(builder.content) || builder.showProgressBar) {
            view.findViewById(R.id.content_parent).setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(builder.content)) {
                TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
                tvContent.setVisibility(View.VISIBLE);
                tvContent.setText(builder.content);
                tvContent.setGravity(builder.contentGravity);
                if (builder.contentColor != -2) {
                    tvContent.setTextColor(builder.contentColor);
                }
            }

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(builder.showProgressBar ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("builder", builder);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(tag, "-------------------onDestroyView");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void initBottom(View view) {

        TextView tvLeft = (TextView) view.findViewById(R.id.tv_left);
        TextView tvRight = (TextView) view.findViewById(R.id.tv_right);

        if (TextUtils.isEmpty(builder.left) && TextUtils.isEmpty(builder.right)) {
            view.findViewById(R.id.dialog_notify_ll).setVisibility(View.GONE);
            view.findViewById(R.id.line_id).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.dialog_notify_ll).setVisibility(View.VISIBLE);
            view.findViewById(R.id.line_id).setVisibility(View.VISIBLE);
            if (builder.leftColor != -2) {
                tvLeft.setTextColor(builder.leftColor);
            }
            if (builder.rightColor != -2) {
                tvRight.setTextColor(builder.rightColor);
            }
            if (!TextUtils.isEmpty(builder.left) && TextUtils.isEmpty(builder.right)) {
                tvLeft.setVisibility(View.VISIBLE);
                tvLeft.setText(builder.left);
            } else if (TextUtils.isEmpty(builder.left) && !TextUtils.isEmpty(builder.right)) {
                tvRight.setVisibility(View.VISIBLE);
                tvRight.setText(builder.right);
            } else {
                tvRight.setVisibility(View.VISIBLE);
                tvRight.setText(builder.right);

                view.findViewById(R.id.divider_id).setVisibility(View.VISIBLE);

                tvLeft.setVisibility(View.VISIBLE);
                tvLeft.setText(builder.left);
            }

            tvLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (builder.leftClickListener != null) {
                        builder.leftClickListener.onClick(v, Jl_Dialog.this);
                    }
                }
            });
            tvRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (builder.rightClickListener != null) {
                        builder.rightClickListener.onClick(v, Jl_Dialog.this);
                    }
                }
            });

        }
    }


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder implements Parcelable {

        private int animations;

        //容器页面
        private int containerRes;
        private View containerView;

        //文字
        private String title;
        private String left;
        private String right;
        private String content;

        //字体颜色
        private int titleColor = -2;
        private int leftColor = -2;
        private int rightColor = -2;
        private int contentColor = -2;

        //位置
        private int contentGravity = Gravity.CENTER;

        //内容布局
        private int contentLayoutRes;
        private View contentLayoutView;

        //背景
        private int backgroundColor = -2;


        private float width = -1; //宽度比0.0~1.0

        private float height = -1; //高度比0.0~1.0

        private boolean cancel = true;

        private boolean showProgressBar = false;


        private OnViewClickListener leftClickListener;


        private OnViewClickListener rightClickListener;

        /**
         * 设置高度比：0.0-1.0
         *
         * @param percent
         * @return
         */
        public Builder height(float percent) {
            this.height = percent;
            return this;
        }

        /**
         * 设置宽度比：0.0-1.0
         *
         * @param percent
         * @return
         */
        public Builder width(float percent) {
            this.width = percent;
            return this;
        }

        public Builder animations(int animatRes) {
            this.animations = animatRes;
            return this;
        }


        public Builder cancel(boolean cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder container(int containerRes) {
            this.containerRes = containerRes;
            return this;
        }

        public Builder containerView(View containerView) {
            this.containerView = containerView;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder left(String left) {
            this.left = left;
            return this;
        }

        public Builder right(String right) {
            this.right = right;
            return this;
        }

        public Builder titleColor(int color) {
            this.titleColor = color;
            return this;
        }

        public Builder contentColor(int color) {
            this.contentColor = color;
            return this;
        }

        public Builder leftColor(int color) {
            this.leftColor = color;
            return this;
        }

        public Builder rightColor(int color) {
            this.rightColor = color;
            return this;
        }

        public Builder contentGravity(int gravity) {
            this.contentGravity = gravity;
            return this;
        }

        public Builder contentLayoutRes(int contentLayoutRes) {
            this.contentLayoutRes = contentLayoutRes;
            return this;
        }

        public Builder contentLayoutView(View contentLayoutView) {
            this.contentLayoutView = contentLayoutView;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }


        public Builder rightClickListener(OnViewClickListener onClickListener) {
            this.rightClickListener = onClickListener;
            return this;
        }

        public Builder leftClickListener(OnViewClickListener onClickListener) {
            this.leftClickListener = onClickListener;
            return this;
        }

        public Builder showProgressBar(boolean showProgressBar) {
            this.showProgressBar = showProgressBar;
            return this;
        }


        public Jl_Dialog build() {
            Jl_Dialog mJl_dialog = new Jl_Dialog();
            mJl_dialog.builder = this;
            return mJl_dialog;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.animations);
            dest.writeInt(this.containerRes);

            dest.writeString(this.title);
            dest.writeString(this.left);
            dest.writeString(this.right);
            dest.writeString(this.content);
            dest.writeInt(this.titleColor);
            dest.writeInt(this.leftColor);
            dest.writeInt(this.rightColor);
            dest.writeInt(this.contentColor);
            dest.writeInt(this.contentGravity);
            dest.writeInt(this.contentLayoutRes);

            dest.writeInt(this.backgroundColor);
            dest.writeByte(this.cancel ? (byte) 1 : (byte) 0);


        }

        public Builder() {
        }

        protected Builder(Parcel in) {
            this.animations = in.readInt();
            this.containerRes = in.readInt();
            this.containerView = in.readParcelable(View.class.getClassLoader());
            this.title = in.readString();
            this.left = in.readString();
            this.right = in.readString();
            this.content = in.readString();
            this.titleColor = in.readInt();
            this.leftColor = in.readInt();
            this.rightColor = in.readInt();
            this.contentColor = in.readInt();
            this.contentGravity = in.readInt();
            this.contentLayoutRes = in.readInt();
            this.contentLayoutView = in.readParcelable(View.class.getClassLoader());
            this.backgroundColor = in.readInt();
            this.cancel = in.readByte() != 0;
            this.leftClickListener = in.readParcelable(OnViewClickListener.class.getClassLoader());
            this.rightClickListener = in.readParcelable(OnViewClickListener.class.getClassLoader());
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }


}

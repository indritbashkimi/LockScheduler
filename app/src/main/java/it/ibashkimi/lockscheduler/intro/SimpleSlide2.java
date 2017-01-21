package it.ibashkimi.lockscheduler.intro;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.slide.ButtonCtaSlide;
import com.heinrichreimersoftware.materialintro.slide.RestorableSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;
import com.heinrichreimersoftware.materialintro.view.parallax.ParallaxSlideFragment;

import java.util.ArrayList;
import java.util.Arrays;

import it.ibashkimi.lockscheduler.model.api.AdminApiHelper;

/**
 * @author Indrit Bashkimi <indrit.bashkimi@gmail.com>
 */

public class SimpleSlide2 implements Slide, RestorableSlide, ButtonCtaSlide {
    private static final int DEFAULT_PERMISSIONS_REQUEST_CODE = 34;
    private SimpleSlide2.SimpleSlideFragment fragment;
    private final CharSequence title;
    @StringRes
    private final int titleRes;
    private final CharSequence description;
    @StringRes
    private final int descriptionRes;
    @DrawableRes
    private final int imageRes;
    @LayoutRes
    private final int layoutRes;
    @ColorRes
    private final int backgroundRes;
    @ColorRes
    private final int backgroundDarkRes;
    private final boolean canGoForward;
    private final boolean canGoBackward;
    private String[] permissions;
    private int permissionsRequestCode;
    private CharSequence buttonCtaLabel = null;
    @StringRes
    private int buttonCtaLabelRes = 0;
    private View.OnClickListener buttonCtaClickListener = null;

    AdminApiHelper adminApiHelper;

    protected SimpleSlide2(Builder builder) {
        this.fragment = SimpleSlideFragment.newInstance(builder.title, builder.titleRes, builder.description, builder.descriptionRes, builder.imageRes, builder.backgroundRes, builder.layoutRes, builder.permissionsRequestCode);
        this.title = builder.title;
        this.titleRes = builder.titleRes;
        this.description = builder.description;
        this.descriptionRes = builder.descriptionRes;
        this.imageRes = builder.imageRes;
        this.layoutRes = builder.layoutRes;
        this.backgroundRes = builder.backgroundRes;
        this.backgroundDarkRes = builder.backgroundDarkRes;
        this.canGoForward = builder.canGoForward;
        this.canGoBackward = builder.canGoBackward;
        this.permissions = builder.permissions;
        this.permissionsRequestCode = builder.permissionsRequestCode;
        this.buttonCtaLabel = builder.buttonCtaLabel;
        this.buttonCtaLabelRes = builder.buttonCtaLabelRes;
        this.buttonCtaClickListener = builder.buttonCtaClickListener;
        this.updatePermissions();
        //this.adminApiHelper = new AdminApiHelper(this.fragment.getActivity());
    }

    public void setAdminApiHelper(AdminApiHelper adminApiHelper) {
        this.adminApiHelper = adminApiHelper;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    public void setFragment(Fragment fragment) {
        if (fragment instanceof SimpleSlide2.SimpleSlideFragment) {
            this.fragment = (SimpleSlide2.SimpleSlideFragment) fragment;
        }

    }

    public int getBackground() {
        return this.backgroundRes;
    }

    public int getBackgroundDark() {
        return this.backgroundDarkRes;
    }

    public boolean canGoForward() {
        return adminApiHelper.isAdminActive();
    }

    public boolean canGoBackward() {
        return this.canGoBackward;
    }

    public View.OnClickListener getButtonCtaClickListener() {
        this.updatePermissions();
        return this.permissions == null ? this.buttonCtaClickListener : new View.OnClickListener() {
            public void onClick(View v) {
                if (SimpleSlide2.this.fragment.getActivity() != null) {
                    ActivityCompat.requestPermissions(SimpleSlide2.this.fragment.getActivity(), SimpleSlide2.this.permissions, SimpleSlide2.this.permissionsRequestCode);
                }

            }
        };
    }

    public CharSequence getButtonCtaLabel() {
        return "Device Manager Permission";
    }

    public int getButtonCtaLabelRes() {
        this.updatePermissions();
        return this.permissions == null ? this.buttonCtaLabelRes : 0;
    }

    private synchronized void updatePermissions() {
        if (this.permissions != null) {
            ArrayList permissionsNotGranted = new ArrayList();
            String[] var2 = this.permissions;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String permission = var2[var4];
                if (this.fragment.getContext() == null || ContextCompat.checkSelfPermission(this.fragment.getContext(), permission) != 0) {
                    permissionsNotGranted.add(permission);
                }
            }

            if (permissionsNotGranted.size() > 0) {
                this.permissions = (String[]) permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
            } else {
                this.permissions = null;
            }
        } else {
            this.permissions = null;
        }

    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            SimpleSlide2 that = (SimpleSlide2) o;
            if (this.titleRes != that.titleRes) {
                return false;
            } else if (this.descriptionRes != that.descriptionRes) {
                return false;
            } else if (this.imageRes != that.imageRes) {
                return false;
            } else if (this.layoutRes != that.layoutRes) {
                return false;
            } else if (this.backgroundRes != that.backgroundRes) {
                return false;
            } else if (this.backgroundDarkRes != that.backgroundDarkRes) {
                return false;
            } else if (this.canGoForward != that.canGoForward) {
                return false;
            } else if (this.canGoBackward != that.canGoBackward) {
                return false;
            } else if (this.permissionsRequestCode != that.permissionsRequestCode) {
                return false;
            } else if (this.buttonCtaLabelRes != that.buttonCtaLabelRes) {
                return false;
            } else {
                label94:
                {
                    if (this.fragment != null) {
                        if (this.fragment.equals(that.fragment)) {
                            break label94;
                        }
                    } else if (that.fragment == null) {
                        break label94;
                    }

                    return false;
                }

                label87:
                {
                    if (this.title != null) {
                        if (this.title.equals(that.title)) {
                            break label87;
                        }
                    } else if (that.title == null) {
                        break label87;
                    }

                    return false;
                }

                if (this.description != null) {
                    if (!this.description.equals(that.description)) {
                        return false;
                    }
                } else if (that.description != null) {
                    return false;
                }

                if (!Arrays.equals(this.permissions, that.permissions)) {
                    return false;
                } else {
                    if (this.buttonCtaLabel != null) {
                        if (this.buttonCtaLabel.equals(that.buttonCtaLabel)) {
                            return this.buttonCtaClickListener != null ? this.buttonCtaClickListener.equals(that.buttonCtaClickListener) : that.buttonCtaClickListener == null;
                        }
                    } else if (that.buttonCtaLabel == null) {
                        return this.buttonCtaClickListener != null ? this.buttonCtaClickListener.equals(that.buttonCtaClickListener) : that.buttonCtaClickListener == null;
                    }

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.fragment != null ? this.fragment.hashCode() : 0;
        result = 31 * result + (this.title != null ? this.title.hashCode() : 0);
        result = 31 * result + this.titleRes;
        result = 31 * result + (this.description != null ? this.description.hashCode() : 0);
        result = 31 * result + this.descriptionRes;
        result = 31 * result + this.imageRes;
        result = 31 * result + this.layoutRes;
        result = 31 * result + this.backgroundRes;
        result = 31 * result + this.backgroundDarkRes;
        result = 31 * result + (this.canGoForward ? 1 : 0);
        result = 31 * result + (this.canGoBackward ? 1 : 0);
        result = 31 * result + Arrays.hashCode(this.permissions);
        result = 31 * result + this.permissionsRequestCode;
        result = 31 * result + (this.buttonCtaLabel != null ? this.buttonCtaLabel.hashCode() : 0);
        result = 31 * result + this.buttonCtaLabelRes;
        result = 31 * result + (this.buttonCtaClickListener != null ? this.buttonCtaClickListener.hashCode() : 0);
        return result;
    }

    public static class SimpleSlideFragment extends ParallaxSlideFragment {
        private static final String ARGUMENT_TITLE = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE";
        private static final String ARGUMENT_TITLE_RES = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE_RES";
        private static final String ARGUMENT_DESCRIPTION = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION";
        private static final String ARGUMENT_DESCRIPTION_RES = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION_RES";
        private static final String ARGUMENT_IMAGE_RES = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_IMAGE_RES";
        private static final String ARGUMENT_BACKGROUND_RES = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_BACKGROUND_RES";
        private static final String ARGUMENT_LAYOUT_RES = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_LAYOUT_RES";
        private static final String ARGUMENT_PERMISSIONS_REQUEST_CODE = "com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_PERMISSIONS_REQUEST_CODE";

        public SimpleSlideFragment() {
        }

        public static SimpleSlide2.SimpleSlideFragment newInstance(CharSequence title, @StringRes int titleRes, CharSequence description, @StringRes int descriptionRes, @DrawableRes int imageRes, @ColorRes int backgroundRes, @LayoutRes int layout, int permissionsRequestCode) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE", title);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE_RES", titleRes);
            arguments.putCharSequence("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION", description);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION_RES", descriptionRes);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_IMAGE_RES", imageRes);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_BACKGROUND_RES", backgroundRes);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_LAYOUT_RES", layout);
            arguments.putInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_PERMISSIONS_REQUEST_CODE", permissionsRequestCode);
            SimpleSlide2.SimpleSlideFragment fragment = new SimpleSlide2.SimpleSlideFragment();
            fragment.setArguments(arguments);
            return fragment;
        }

        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setRetainInstance(true);
            this.updateNavigation();
        }

        public void onResume() {
            super.onResume();
            this.updateNavigation();
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle arguments = this.getArguments();
            View fragment = inflater.inflate(arguments.getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_LAYOUT_RES", com.heinrichreimersoftware.materialintro.R.layout.fragment_simple_slide), container, false);
            TextView titleView = (TextView) fragment.findViewById(com.heinrichreimersoftware.materialintro.R.id.mi_title);
            TextView descriptionView = (TextView) fragment.findViewById(com.heinrichreimersoftware.materialintro.R.id.mi_description);
            ImageView imageView = (ImageView) fragment.findViewById(com.heinrichreimersoftware.materialintro.R.id.mi_image);
            CharSequence title = arguments.getCharSequence("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE");
            int titleRes = arguments.getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_TITLE_RES");
            CharSequence description = arguments.getCharSequence("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION");
            int descriptionRes = arguments.getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_DESCRIPTION_RES");
            int imageRes = arguments.getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_IMAGE_RES");
            int backgroundRes = arguments.getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_BACKGROUND_RES");
            if (titleView != null) {
                if (title != null) {
                    titleView.setText(title);
                    titleView.setVisibility(View.VISIBLE);
                } else if (titleRes != 0) {
                    titleView.setText(titleRes);
                    titleView.setVisibility(View.VISIBLE);
                } else {
                    titleView.setVisibility(View.GONE);
                }
            }

            if (descriptionView != null) {
                if (description != null) {
                    descriptionView.setText(description);
                    descriptionView.setVisibility(View.VISIBLE);
                } else if (descriptionRes != 0) {
                    descriptionView.setText(descriptionRes);
                    descriptionView.setVisibility(View.VISIBLE);
                } else {
                    descriptionView.setVisibility(View.GONE);
                }
            }

            if (imageView != null) {
                if (imageRes != 0) {
                    imageView.setImageResource(imageRes);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }

            int textColorPrimary;
            int textColorSecondary;
            if (backgroundRes != 0 && ColorUtils.calculateLuminance(ContextCompat.getColor(this.getContext(), backgroundRes)) < 0.6D) {
                textColorPrimary = ContextCompat.getColor(this.getContext(), com.heinrichreimersoftware.materialintro.R.color.mi_text_color_primary_dark);
                textColorSecondary = ContextCompat.getColor(this.getContext(), com.heinrichreimersoftware.materialintro.R.color.mi_text_color_secondary_dark);
            } else {
                textColorPrimary = ContextCompat.getColor(this.getContext(), com.heinrichreimersoftware.materialintro.R.color.mi_text_color_primary_light);
                textColorSecondary = ContextCompat.getColor(this.getContext(), com.heinrichreimersoftware.materialintro.R.color.mi_text_color_secondary_light);
            }

            if (titleView != null) {
                titleView.setTextColor(textColorPrimary);
            }

            if (descriptionView != null) {
                descriptionView.setTextColor(textColorSecondary);
            }

            return fragment;
        }

        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            int permissionsRequestCode = this.getArguments() == null ? 34 : this.getArguments().getInt("com.heinrichreimersoftware.materialintro.SimpleFragment.ARGUMENT_PERMISSIONS_REQUEST_CODE", 34);
            if (requestCode == permissionsRequestCode) {
                this.updateNavigation();
            }

        }
    }

    public static class Builder {
        @ColorRes
        private int backgroundRes = 0;
        @ColorRes
        private int backgroundDarkRes = 0;
        private CharSequence title = null;
        @StringRes
        private int titleRes = 0;
        private CharSequence description = null;
        @StringRes
        private int descriptionRes = 0;
        @DrawableRes
        private int imageRes = 0;
        @LayoutRes
        private int layoutRes;
        private boolean canGoForward;
        private boolean canGoBackward;
        private String[] permissions;
        private CharSequence buttonCtaLabel;
        @StringRes
        private int buttonCtaLabelRes;
        private View.OnClickListener buttonCtaClickListener;
        private int permissionsRequestCode;

        public Builder() {
            this.layoutRes = com.heinrichreimersoftware.materialintro.R.layout.fragment_simple_slide;
            this.canGoForward = true;
            this.canGoBackward = true;
            this.permissions = null;
            this.buttonCtaLabel = null;
            this.buttonCtaLabelRes = 0;
            this.buttonCtaClickListener = null;
            this.permissionsRequestCode = 34;
        }

        public SimpleSlide2.Builder background(@ColorRes int backgroundRes) {
            this.backgroundRes = backgroundRes;
            return this;
        }

        public SimpleSlide2.Builder backgroundDark(@ColorRes int backgroundDarkRes) {
            this.backgroundDarkRes = backgroundDarkRes;
            return this;
        }

        public SimpleSlide2.Builder title(CharSequence title) {
            this.title = title;
            this.titleRes = 0;
            return this;
        }

        public SimpleSlide2.Builder titleHtml(String titleHtml) {
            if (Build.VERSION.SDK_INT >= 24) {
                this.title = Html.fromHtml(titleHtml, 0);
            } else {
                this.title = Html.fromHtml(titleHtml);
            }

            this.titleRes = 0;
            return this;
        }

        public SimpleSlide2.Builder title(@StringRes int titleRes) {
            this.titleRes = titleRes;
            this.title = null;
            return this;
        }

        public SimpleSlide2.Builder description(CharSequence description) {
            this.description = description;
            this.descriptionRes = 0;
            return this;
        }

        public SimpleSlide2.Builder descriptionHtml(String descriptionHtml) {
            if (Build.VERSION.SDK_INT >= 24) {
                this.description = Html.fromHtml(descriptionHtml, 0);
            } else {
                this.description = Html.fromHtml(descriptionHtml);
            }

            this.descriptionRes = 0;
            return this;
        }

        public SimpleSlide2.Builder description(@StringRes int descriptionRes) {
            this.descriptionRes = descriptionRes;
            this.description = null;
            return this;
        }

        public SimpleSlide2.Builder image(@DrawableRes int imageRes) {
            this.imageRes = imageRes;
            return this;
        }

        public SimpleSlide2.Builder layout(@LayoutRes int layoutRes) {
            this.layoutRes = layoutRes;
            return this;
        }

        public SimpleSlide2.Builder scrollable(boolean scrollable) {
            this.layoutRes = scrollable ? com.heinrichreimersoftware.materialintro.R.layout.fragment_simple_slide_scrollable : com.heinrichreimersoftware.materialintro.R.layout.fragment_simple_slide;
            return this;
        }

        public SimpleSlide2.Builder canGoForward(boolean canGoForward) {
            this.canGoForward = canGoForward;
            return this;
        }

        public SimpleSlide2.Builder canGoBackward(boolean canGoBackward) {
            this.canGoBackward = canGoBackward;
            return this;
        }

        public SimpleSlide2.Builder permissions(String[] permissions) {
            this.permissions = permissions;
            return this;
        }

        public SimpleSlide2.Builder permission(String permission) {
            this.permissions = new String[]{permission};
            return this;
        }

        public SimpleSlide2.Builder permissionsRequestCode(int permissionsRequestCode) {
            this.permissionsRequestCode = permissionsRequestCode;
            return this;
        }

        public SimpleSlide2.Builder buttonCtaLabel(CharSequence buttonCtaLabel) {
            this.buttonCtaLabel = buttonCtaLabel;
            this.buttonCtaLabelRes = 0;
            return this;
        }

        public SimpleSlide2.Builder buttonCtaLabelHtml(String buttonCtaLabelHtml) {
            if (Build.VERSION.SDK_INT >= 24) {
                this.buttonCtaLabel = Html.fromHtml(buttonCtaLabelHtml, 0);
            } else {
                this.buttonCtaLabel = Html.fromHtml(buttonCtaLabelHtml);
            }

            this.buttonCtaLabelRes = 0;
            return this;
        }

        public SimpleSlide2.Builder buttonCtaLabel(@StringRes int buttonCtaLabelRes) {
            this.buttonCtaLabelRes = buttonCtaLabelRes;
            this.buttonCtaLabel = null;
            return this;
        }

        public SimpleSlide2.Builder buttonCtaClickListener(View.OnClickListener buttonCtaClickListener) {
            this.buttonCtaClickListener = buttonCtaClickListener;
            return this;
        }

        public SimpleSlide2 build() {
            if (this.backgroundRes == 0) {
                throw new IllegalArgumentException("You must set a background.");
            } else {
                return new SimpleSlide2(this);
            }
        }
    }
}


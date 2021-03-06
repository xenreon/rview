/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.fragments;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.ruesga.rview.R;
import com.ruesga.rview.databinding.TagEditDialogBinding;
import com.ruesga.rview.widget.TagEditTextView;
import com.ruesga.rview.widget.TagEditTextView.Tag;

import java.util.ArrayList;

public class TagEditDialogFragment extends RevealDialogFragment {

    public static final String TAG = "TagEditDialogFragment";

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_TAGS = "tags";
    private static final String EXTRA_ACTION = "action";

    private static final String EXTRA_REQUEST_CODE = "request_code";

    private static final String STATE_TAGS = "state:tags";

    @Keep
    public static class Model {
        public String subtitle;
        public String hint;
    }

    public interface OnTagEditChanged {
        void onTagEditChanged(int requestCode, Tag[] newTags);
    }


    public static TagEditDialogFragment newInstance(
            String title, Tag[] tags, String action, View anchor, int requestCode) {
        TagEditDialogFragment fragment = new TagEditDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_TITLE, title);
        ArrayList<String> serializedTags = new ArrayList<>(tags.length);
        for (Tag tag : tags) {
            serializedTags.add(tag.toPlainTag().toString());
        }
        arguments.putStringArrayList(EXTRA_TAGS, serializedTags);
        arguments.putString(EXTRA_ACTION, action);
        arguments.putParcelable(EXTRA_ANCHOR, computeViewOnScreen(anchor));
        arguments.putInt(EXTRA_REQUEST_CODE, requestCode);
        fragment.setArguments(arguments);
        return fragment;
    }

    private int mRequestCode;

    private TagEditDialogBinding mBinding;
    private final Model mModel = new Model();

    public TagEditDialogFragment() {
    }

    @Override
    public void buildDialog(AlertDialog.Builder builder, Bundle savedInstanceState) {
        //noinspection ConstantConditions
        String title = getArguments().getString(EXTRA_TITLE);
        String action = getArguments().getString(EXTRA_ACTION);
        if (TextUtils.isEmpty(action)) {
            action = getString(R.string.action_save);
        }

        LayoutInflater inflater = LayoutInflater.from(builder.getContext());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tag_edit_dialog, null, true);
        if (savedInstanceState == null) {
            ArrayList<String> tags = getArguments().getStringArrayList(EXTRA_TAGS);
            if (tags != null) {
                int count = tags.size();
                Tag[] t = new Tag[count];
                for (int i = 0; i < count; i++) {
                    t[i] = new Tag(TagEditTextView.TAG_MODE.HASH, tags.get(i));
                }
                mBinding.tagsEditor.setTags(t);
            }
        } else {
            mBinding.tagsEditor.fromPlainTags(savedInstanceState.getString(STATE_TAGS));
        }
        mBinding.setModel(mModel);

        builder.setTitle(title)
                .setView(mBinding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(action, (dialog, which) -> performEditChanged());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        mRequestCode = getArguments().getInt(EXTRA_REQUEST_CODE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TAGS, mBinding.tagsEditor.toPlainTags());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDialogReveled() {
        mBinding.tagsEditor.clearFocus();
        mBinding.tagsEditor.requestFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
    }

    private void performEditChanged() {
        mBinding.tagsEditor.computeTags(null);
        Activity a = getActivity();
        Fragment f = getParentFragment();
        if (f instanceof OnTagEditChanged) {
            ((OnTagEditChanged) f).onTagEditChanged(mRequestCode, mBinding.tagsEditor.getTags());
        } else if (a instanceof OnTagEditChanged) {
            ((OnTagEditChanged) a).onTagEditChanged(mRequestCode, mBinding.tagsEditor.getTags());
        }
    }
}

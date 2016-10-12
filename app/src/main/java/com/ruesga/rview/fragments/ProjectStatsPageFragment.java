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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruesga.rview.R;
import com.ruesga.rview.databinding.ProjectDetailsViewBinding;
import com.ruesga.rview.gerrit.GerritApi;
import com.ruesga.rview.gerrit.model.ProjectInfo;
import com.ruesga.rview.misc.ModelHelper;
import com.ruesga.rview.preferences.Constants;

import rx.Observable;

public class ProjectStatsPageFragment extends StatsPageFragment<ProjectInfo> {

    private static final String TAG = "ProjectStatsPageFragment";

    private ProjectDetailsViewBinding mBinding;
    private String mProjectName;

    public static ProjectStatsPageFragment newFragment(String projectName) {
        ProjectStatsPageFragment fragment = new ProjectStatsPageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_ID, projectName);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProjectName = getArguments().getString(Constants.EXTRA_ID);
    }

    @Override
    public View inflateDetails(LayoutInflater inflater, @Nullable ViewGroup container) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.project_details_view, container, false);
        return mBinding.getRoot();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Observable<ProjectInfo> fetchDetails() {
        GerritApi api = ModelHelper.getGerritApi(getContext());
        return api.getProject(mProjectName);
    }

    @Override
    public void bindDetails(ProjectInfo result) {
        mBinding.setModel(result);
        mBinding.executePendingBindings();
    }

    @Override
    public String getStatsFragmentTag() {
        return TAG;
    }
}
package com.pij.noopetal.example;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;
import static com.pij.noopetal.example.SimpleAdapter.ViewHolder;

@RunWith(RobolectricTestRunner.class)
//@Config(constants = BuildConfig.class,
//        packageName = "com.pij.noopetal.example",
//        manifest = "src/main/AndroidManifest.xml")
public class SimpleAdapterTest {

    @Test
    public void verifyViewHolderViews() {
        Context context = RuntimeEnvironment.application;

        @SuppressLint("InflateParams")
        View root = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
        ViewHolder holder = new ViewHolder(root);

        assertThat(holder.word.getId()).isEqualTo(R.id.word);
        assertThat(holder.length.getId()).isEqualTo(R.id.length);
        assertThat(holder.position.getId()).isEqualTo(R.id.position);
    }
}

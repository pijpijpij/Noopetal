package com.pij.noopetal.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.butterknife.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static com.pij.noopetal.example.SimpleAdapter.ViewHolder;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = "src/main/AndroidManifest.xml")
public class SimpleAdapterTest {
  @Test public void verifyViewHolderViews() {
    Context context = Robolectric.application;

    View root = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
    ViewHolder holder = new ViewHolder(root);

    assertThat(holder.word.getId()).isEqualTo(R.id.word);
    assertThat(holder.length.getId()).isEqualTo(R.id.length);
    assertThat(holder.position.getId()).isEqualTo(R.id.position);
  }
}

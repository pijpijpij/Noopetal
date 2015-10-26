package com.pij.noopetal.example;

import com.example.butterknife.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import butterknife.ButterKnife;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = "src/main/AndroidManifest.xml")
public class SimpleActivityTest {

    @Test
    public void verifyContentViewBinding() {
        SimpleActivity activity = Robolectric.buildActivity(SimpleActivity.class) //
                .create() //
                .get();

        assertThat(activity.title.getId()).isEqualTo(R.id.title);
        assertThat(activity.subtitle.getId()).isEqualTo(R.id.subtitle);
        assertThat(activity.hello.getId()).isEqualTo(R.id.hello);
        assertThat(activity.listOfThings.getId()).isEqualTo(R.id.list_of_things);
        assertThat(activity.footer.getId()).isEqualTo(R.id.footer);

        ButterKnife.unbind(activity);
        assertThat(activity.title).isNull();
        assertThat(activity.subtitle).isNull();
        assertThat(activity.hello).isNull();
        assertThat(activity.listOfThings).isNull();
        assertThat(activity.footer).isNull();
    }
}

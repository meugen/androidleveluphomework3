package ua.meugen.android.levelup.homework3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ua.meugen.android.levelup.homework3.views.TransformationContentView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TransformationContentView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.first_transformation).setOnClickListener(this);
        findViewById(R.id.second_transformation).setOnClickListener(this);
        findViewById(R.id.third_transformation).setOnClickListener(this);

        this.contentView = (TransformationContentView) findViewById(R.id.content);
    }

    @Override
    public void onClick(final View view) {
        final int viewId = view.getId();
        if (viewId == R.id.first_transformation) {
            this.contentView.setTransformation(TransformationContentView.INVERT_GREEN_AND_BLUE);
        } else if (viewId == R.id.second_transformation) {
            this.contentView.setTransformation(TransformationContentView.CUT_IMAGE);
        } else if (viewId == R.id.third_transformation) {
            this.contentView.setTransformation(TransformationContentView.PUT_GRADIENT);
        }
    }
}

package in.digistorm.aksharam;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.digistorm.aksharam.util.Log;

public class LanguageInfoFragment extends Fragment {
    private final String logTag = "LanguageInfoFragment";

    public static LanguageInfoFragment newInstance(String info) {
        LanguageInfoFragment lif = new LanguageInfoFragment();

        Bundle args = new Bundle();
        args.putString("info", info);
        lif.setArguments(args);

        return lif;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.language_info, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String info = null;
        if(getArguments() != null)
            info = getArguments().getString("info");

        if(info == null)
            Log.d(logTag, "Info is null");

        ((TextView) view.findViewById(R.id.languageInfoTV)).setText(Html.fromHtml(info));
    }
}

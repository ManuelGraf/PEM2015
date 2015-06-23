package pem.yara.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pem.yara.R;
import pem.yara.RunActivity;


public class HomeScreenFragment extends Fragment {
        public static final String ARG_OBJECT = "object";
        private Button btnStartRunning;
        private View mRootView;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_home_screen, container, false);


            btnStartRunning = (Button)rootView.findViewById(R.id.btnStartRunning);
            btnStartRunning.setOnClickListener(startRunListener);
            mRootView = rootView;
            Bundle args = getArguments();

            return rootView;
        }

        View.OnClickListener startRunListener = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RunActivity.class);
                getActivity().startActivity(intent);
            }
        };

}

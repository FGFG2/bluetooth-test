package com.tobyrich.dev.lib_smartlink_fgfg;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.TextView;

import com.tobyrich.dev.lib.connection.events.RuderEvent;
import com.tobyrich.dev.lib.connection.events.RuderResult;
import com.tobyrich.dev.lib.connection.events.MotorEvent;
import com.tobyrich.dev.lib.connection.events.MotorResult;
import com.tobyrich.dev.lib.connection.events.BatteryResult;

import de.greenrobot.event.EventBus;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BluetoothListFragment extends Fragment {

    private static final String TAG = "tr.test.BluetoothListFragment";

    private OnFragmentInteractionListener mListener;

    private Activity parent = null;

    private SeekBar ruderControl = null;
    private SeekBar motorControl = null;
    private ProgressBar batteryBar = null;

    private TextView batteryBarLabel = null;

    private EventBus bus = EventBus.getDefault();


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public static BluetoothListFragment newInstance(Activity a) {
        BluetoothListFragment fragment = new BluetoothListFragment();
        Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetoothlist, container, false);

        ruderControl = (SeekBar) view.findViewById(R.id.ruderControl);
        ruderControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                bus.post(new RuderEvent(progressChanged-126));
                Toast.makeText(getActivity(), "Ruder:" + (progressChanged-126),Toast.LENGTH_SHORT).show();
            }
        });
        motorControl = (SeekBar) view.findViewById(R.id.motorControl);
        motorControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                bus.post(new MotorEvent(progressChanged));
                Toast.makeText(getActivity(), "Motor:" + progressChanged,Toast.LENGTH_SHORT).show();
            }
        });
        batteryBar = (ProgressBar) view.findViewById(R.id.batteryBar);
        batteryBarLabel = (TextView) view.findViewById(R.id.batteryBarLabel);
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
            ((MainActivity) activity).onSectionAttached(1);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }


    /*
     * Events
     */

    public void onEvent(BatteryResult evt){
        int value = evt.getResult();
        batteryBar.setProgress(value);
        batteryBarLabel.setText("Battery:\n " + value + " % ");
        Log.d(TAG, "BatteryResult " + value);
        Toast.makeText(getActivity(), "Battery: " + value+" %",Toast.LENGTH_SHORT).show();
    }

    public void onEvent(RuderResult evt){
        ruderControl.setProgress(evt.getResult()+126);
    }
    public void onEvent(MotorResult evt){
        motorControl.setProgress(evt.getResult());
    }

}

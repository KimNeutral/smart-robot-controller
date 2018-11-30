package dgsw.hs.kr.smartrobot.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import dgsw.hs.kr.smartrobot.R;
import dgsw.hs.kr.smartrobot.adapter.DeviceItemRecyclerViewAdapter;
import dgsw.hs.kr.smartrobot.model.DeviceInfo;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DeviceFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    private DeviceItemRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceFragment() {
    }

    @SuppressWarnings("unused")
    public static DeviceFragment newInstance(OnListFragmentInteractionListener mListener) {
        DeviceFragment fragment = new DeviceFragment();
        fragment.mListener = mListener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            adapter = new DeviceItemRecyclerViewAdapter(new ArrayList<>(), mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addItem(DeviceInfo item) {
        adapter.addItem(item);
    }

    public void clearItem() {
        adapter.clearItem();
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DeviceInfo item);
    }
}

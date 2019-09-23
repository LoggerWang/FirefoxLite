package org.mozilla.rocket.msrp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.Lazy
import org.mozilla.focus.R
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.getActivityViewModel
import javax.inject.Inject

class MissionDetailFragment : Fragment() {

    private lateinit var viewModel: MissionViewModel

    @Inject
    lateinit var missionViewModelCreator: Lazy<MissionViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate: View = inflater.inflate(R.layout.fragment_mission_detail, container, false)
//        inflate.findViewById<View>(R.id.dmd_sign_in).setOnClickListener {
//            val isOnMain = activity is MainActivity
//            if (isOnMain) {
//                (activity as MainActivity).loginFxa()
//            }
//        }
        return inflate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getActivityViewModel(missionViewModelCreator)
//        viewModel.missions.observe(viewLifecycleOwner, Observer {
//            Toast.makeText(context, "size:${it?.size}", Toast.LENGTH_LONG).show()
//        })
//        viewModel.loadMissions()
    }

    private fun openFxLoginPage(prevUid: String) {
        findNavController().navigate(MissionDetailFragmentDirections.actionMissionDetailDestToFxLoginDest(prevUid))
    }

    private fun openRewardPage() {
        findNavController().navigate(MissionDetailFragmentDirections.actionMissionDetailDestToRewardDest())
    }
}
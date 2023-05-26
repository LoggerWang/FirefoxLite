package org.mozilla.rocket.msrp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import dagger.Lazy
import org.mozilla.focus.R
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.getActivityViewModel
import org.mozilla.rocket.msrp.data.Mission
import org.mozilla.rocket.msrp.ui.adapter.RewardTabsAdapter
import javax.inject.Inject
import com.google.android.material.tabs.TabLayout


class RewardFragment : Fragment() {

    @Inject
    lateinit var missionViewModelCreator: Lazy<MissionViewModel>

    private lateinit var viewModel: MissionViewModel
    private lateinit var adapter: RewardTabsAdapter
    private lateinit var view_pager: ViewPager
    private lateinit var reward_tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
        viewModel = getActivityViewModel(missionViewModelCreator)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_pager = view.findViewById(R.id.view_pager)
        reward_tabs = view.findViewById(R.id.reward_tabs)
        initViewPager()
        initTabLayout()
        observeNavigation()

    }

    private fun initViewPager() {
        adapter = RewardTabsAdapter(childFragmentManager, requireActivity().resources)
        view_pager.apply {
            adapter = this@RewardFragment.adapter
        }
    }

    private fun initTabLayout() {
        reward_tabs.setupWithViewPager(view_pager)
    }

    private fun observeNavigation() {
        viewModel.run {
            openMissionDetailPage.observe(viewLifecycleOwner, Observer {
                openMissionDetailPage(it)
            })
            openRedeemPage.observe(viewLifecycleOwner, Observer {
                openRedeemPage(it)
            })
        }
    }

    private fun openMissionDetailPage(mission: Mission) {
        findNavController().navigate(RewardFragmentDirections.actionRewardDestToMissionDetailDest(mission))
    }

    private fun openRedeemPage(mission: Mission) {
        findNavController().navigate(RewardFragmentDirections.actionRewardDestToRedeemDest(mission))
    }
}
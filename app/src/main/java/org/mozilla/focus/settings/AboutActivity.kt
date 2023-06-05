package org.mozilla.focus.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.focus.R

class AboutActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var mAboutFragment : AboutFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initView()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    companion object{
        fun launch(
            activity: Activity, prepage : String
        ) {
            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("pve_pre", prepage)
            activity.startActivity(intent)
        }
    }


    private fun initView() {
        findViewById<ImageView>(R.id.iv_page_back).setOnClickListener(this)
        initFragment()
    }

    private fun initFragment(){
        mAboutFragment = AboutFragment.newInstance()
        supportFragmentManager.beginTransaction()?.add(R.id.fl_setting,
            mAboutFragment!!
        )?.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        back()
    }

    fun back(){
        finish()
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_page_back ->{back()}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
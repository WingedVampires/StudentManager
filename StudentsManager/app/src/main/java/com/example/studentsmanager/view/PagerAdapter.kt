package com.example.studentsmanager.view

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var mCurrentfragment: Fragment? = null
    private val fragmentsOfWaterfall = mutableListOf<Fragment>()
    private val fragmentsTitles = mutableListOf<String>()

    fun add(fragment: Fragment, title: String) {
        fragmentsOfWaterfall.add(fragment)
        fragmentsTitles.add(title)
    }

    override fun getItem(position: Int): Fragment = fragmentsOfWaterfall[position]

    override fun getCount(): Int = fragmentsOfWaterfall.size

    override fun getPageTitle(position: Int): CharSequence? = fragmentsTitles[position]

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        mCurrentfragment = `object` as Fragment
        super.setPrimaryItem(container, position, `object`)
    }

    fun getCurrentFragment(): Fragment? = mCurrentfragment
}
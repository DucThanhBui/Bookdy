package com.example.bookdy.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LifecycleDelegates(private val fragment: Fragment) : DefaultLifecycleObserver {

    private class ViewLifecycleAwareVar<T : Any> : ReadWriteProperty<Fragment, T> {
        var nullableValue: T? = null

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            return nullableValue
                ?: throw IllegalStateException("Lifecycle-aware value not available at the moment.")
        }

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
            nullableValue = value
        }
    }

    private val viewLifecycleValues: MutableList<ViewLifecycleAwareVar<*>> =
        mutableListOf()

    override fun onCreate(owner: LifecycleOwner) {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    onViewDestroy()
                }
            })
        }
    }

    private fun onViewDestroy() {
        viewLifecycleValues.forEach { delegate ->
            delegate.nullableValue = null
        }
    }

    fun <T : Any> viewLifecycleAware(): ReadWriteProperty<Fragment, T> {
        val delegate = ViewLifecycleAwareVar<T>()
        viewLifecycleValues.add(delegate as ViewLifecycleAwareVar<*>)
        return delegate
    }
}

fun <T : Any> Fragment.viewLifecycle() =
    LifecycleDelegates(this).viewLifecycleAware<T>()

package `in`.digistorm.aksharam.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class LabelledArrayAdapter<T>(
    context: Context, resource: Int, textViewResourceId: Int, objects: List<T>,
    private val labelTextViewResourceId: Int, private val label: String
) : ArrayAdapter<T>(context, resource, textViewResourceId, objects){

    private val logTag: String = this.javaClass.simpleName

    private fun setLabel(view: View) {
        logDebug(logTag, "Setting label $label")
        view.findViewById<TextView>(labelTextViewResourceId).text = label
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        setLabel(view)
        return view
    }
}
/*
 * Copyright (c) 2022 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package `in`.digistorm.aksharam.activities.main.fragments.settings

import `in`.digistorm.aksharam.activities.main.util.deleteFile
import `in`.digistorm.aksharam.activities.main.util.downloadFile
import `in`.digistorm.aksharam.activities.main.util.writeTofile
import `in`.digistorm.aksharam.databinding.SettingsLanguageListItemBinding
import `in`.digistorm.aksharam.activities.main.util.logDebug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class LanguageListAdapter: ListAdapter<AksharamFile, LanguageListAdapter.LanguageListItemViewHolder>(
    LanguageListDiffer()
) {
    private val logTag = javaClass.simpleName

    private class LanguageListDiffer: DiffUtil.ItemCallback<AksharamFile>() {
        private val logTag = javaClass.simpleName

        override fun areItemsTheSame(
            oldItem: AksharamFile,
            newItem: AksharamFile
        ): Boolean {
            logDebug(logTag, "areItemsTheSame: oldItem: $oldItem newItem $newItem")
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: AksharamFile,
            newItem: AksharamFile
        ): Boolean {
            logDebug(logTag, "areContentsTheSame: oldItem: $oldItem newItem $newItem")
            return oldItem == newItem
        }
    }

    class LanguageListItemViewHolder(val listItemBinding: SettingsLanguageListItemBinding)
        : RecyclerView.ViewHolder(listItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageListItemViewHolder {
        logDebug(logTag, "Initialising adapter for managing language data file list...")
        val listItemBinding = SettingsLanguageListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageListItemViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: LanguageListItemViewHolder, position: Int) {
        logDebug(logTag, "Binding ViewHolder for \"${getItem(position)}\" at position: $position")
        holder.listItemBinding.apply {
            deleteLanguage.setOnClickListener(deleteClickListener(holder, position))
            downloadLanguage.setOnClickListener(downloadClickListener(holder, position))
            language.text = getItem(position).displayName()
            updateDeleteDownloadView(holder, getItem(position).isDownloaded)
        }
    }

    private fun deleteClickListener(holder: LanguageListItemViewHolder, position: Int): (View) -> Unit {
        return {
            holder.listItemBinding.root.findViewTreeLifecycleOwner()!!.lifecycleScope.launch {
                deleteFile(getItem(position).onlineLanguageFile?.name
                    ?: getItem(position).localFileName!!, holder.listItemBinding.root.context)
            }
            getItem(position).isDownloaded = false
            notifyItemChanged(position)
        }
    }

    private fun downloadClickListener(holder: LanguageListItemViewHolder, position: Int): (View) -> Unit {
        return {
            holder.listItemBinding.root.findViewTreeLifecycleOwner()!!.lifecycleScope.launch {
                holder.listItemBinding.downloadLanguage.visibility = View.GONE
                holder.listItemBinding.progressCircular.visibility = View.VISIBLE
                val fileContent = downloadFile(
                    url = getItem(position).onlineLanguageFile?.download_url!!,
                )
                writeTofile(
                    fileName = getItem(position).onlineLanguageFile?.name!!,
                    fileContent!!,
                    holder.listItemBinding.root.context
                )
                getItem(position).isDownloaded = true
                holder.listItemBinding.progressCircular.visibility = View.GONE
                notifyItemChanged(position)
            }
        }
    }

    private fun updateDeleteDownloadView(
        holder: LanguageListItemViewHolder, isDownloaded: Boolean) {
        holder.listItemBinding.apply {
            if(isDownloaded) {
                deleteLanguage.visibility = View.VISIBLE
                downloadLanguage.visibility = View.GONE
            } else {
                deleteLanguage.visibility = View.GONE
                downloadLanguage.visibility = View.VISIBLE
            }
        }
    }
}
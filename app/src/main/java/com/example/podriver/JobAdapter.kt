package com.example.podriver

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class JobAdapter internal constructor(private var context: Context, private val onJobListener: OnJobListener) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>(), Filterable {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    //    private val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
    private var jobList = ArrayList<Job>()
    private var jobListFiltered = ArrayList<Job>()

    interface OnJobListener {
        fun onJobClick(position: Int)
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val status: TextView = itemView.findViewById(R.id.status)
        val number: TextView = itemView.findViewById(R.id.number)
        val loading: TextView = itemView.findViewById(R.id.loading)
        val destination: TextView = itemView.findViewById(R.id.destination)
        val cargo: TextView = itemView.findViewById(R.id.cargo)
        val take: Button = itemView.findViewById(R.id.take)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val itemView = inflater.inflate(R.layout.recycleview_item, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val current = jobListFiltered[position]

        holder.status.text = current.status
        holder.number.text = "00" + current.number.toString()
        holder.loading.text = "From: \n" + current.loading
        holder.destination.text = "To: \n" + current.destination
        holder.cargo.text = "Cargo: " + current.cargo

        if (current.status == "IN_PROGRESS") {
            holder.take.visibility = Button.VISIBLE
            holder.take.text = "Finish"
        } else if (current.status == "FINISHED") {
            holder.take.visibility = Button.INVISIBLE
        } else if (current.status == "ASSIGNED"){
            holder.take.visibility = Button.VISIBLE
            holder.take.text = "Take"
        }

        holder.take.setOnClickListener {
            onJobListener.onJobClick(position)
        }
    }

    internal fun setJobs(jobs: Iterable<Job>) {
        this.jobList.clear()
        jobs.forEach { job -> if (job.status == "IN_PROGRESS") jobList.add(job) }
        jobs.forEach { job -> if (job.status != "IN_PROGRESS" && job.status != "FINISHED") jobList.add(job) }
        jobs.forEach { job -> if (job.status == "FINISHED") jobList.add(job) }

        this.jobList.reversed()
        this.jobListFiltered.clear()
        jobs.forEach { job -> if (job.status == "IN_PROGRESS") jobListFiltered.add(job) }
        jobs.forEach { job -> if (job.status != "IN_PROGRESS" && job.status != "FINISHED") jobListFiltered.add(job) }
        jobs.forEach { job -> if (job.status == "FINISHED") jobListFiltered.add(job) }

        this.jobListFiltered.reversed()
        notifyDataSetChanged()
    }

    override fun getItemCount() = jobListFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase()
                if (charString.isEmpty()) {
                    jobListFiltered = jobList
                } else {
                    val filteredList = ArrayList<Job>()
                    for (row in jobList) {
                        if (row.status.toLowerCase().contains(charString) ||
                            row.number.toString().toLowerCase().contains(charString) ||
                            row.loading.contains(charString) ||
                            row.destination.contains(charString) ||
                            row.cargo.contains(charString)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    jobListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = jobListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values is ArrayList<*>) {
                    jobListFiltered = results.values as ArrayList<Job>
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return jobListFiltered[position].id.hashCode().toLong()
    }
}


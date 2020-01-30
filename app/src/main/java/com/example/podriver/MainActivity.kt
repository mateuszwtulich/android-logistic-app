package com.example.podriver

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recycleview_item.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), JobAdapter.OnJobListener{

    private lateinit var jobList : ArrayList<Job>
    private lateinit var jobDtoList : ArrayList<JobDto>
    private lateinit var adapter: JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = JobAdapter(baseContext, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(baseContext)

        val url = "http://10.0.2.2:8080/drivers/8/jobs"

        AsyncTaskGetHandleJson().execute(url)

        logo.setOnClickListener {
            AsyncTaskGetHandleJson().execute(url)
            Toast.makeText(baseContext, "Jobs have been refreshed!", Toast.LENGTH_LONG).show()
        }
    }


    inner class AsyncTaskGetHandleJson: AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String {

            var text: String
            val connection = URL(params[0]).openConnection() as HttpURLConnection
            try{
                connection.connect()
                text = connection.inputStream.use { it.reader().use { it.readText() } }
            }finally {
                connection.disconnect()
            }

            return text
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            handleJson(result)
        }
    }

    fun handleJson(jsonString: String?){
        val jsonArray = JSONArray(jsonString)

        val list = ArrayList<Job>()
        val listDto = ArrayList<JobDto>()

        var x = 0
        while (x <jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(x)

            list.add(Job(
                jsonObject.getLong("id"),
                jsonObject.getLong("number"),
                jsonObject.getString("status"),
                jsonObject.getString("loading"),
                jsonObject.getString("destination"),
                jsonObject.getString("cargo")
            ))

            listDto.add(JobDto(
                jsonObject.getLong("number"),
                jsonObject.getString("status"),
                jsonObject.getString("date"),
                jsonObject.getString("commissionedParty"),
                jsonObject.getLong("principalId"),
                jsonObject.getLong("managerId"),
                jsonObject.getLong("driverId"),
                jsonObject.getLong("cargoId"),
                jsonObject.getLong("unloadingId"),
                jsonObject.getLong("loadingId"),
                jsonObject.getString("placeOfIssue"),
                jsonObject.getDouble("weight"),
                jsonObject.getInt("payRate"),
                jsonObject.getString("comment")
            ))
            val comment: String? = null
            x++
        }

        adapter.setJobs(list)
        adapter.notifyDataSetChanged()
        jobList = list
        jobDtoList = listDto
    }

    override fun onJobClick(position: Int) {
        val jobId = adapter.getItemId(position)
        val number = jobList.find { it.id == jobId }!!.number

        val job = jobList.find { it.id == jobId }
        if(job!!.status == "ASSIGNED") {
            AsyncTaskPutHandleJson().execute(jobId.toString(), number.toString(), "IN_PROGRESS")
            Toast.makeText(baseContext, "Job is in progress now!", Toast.LENGTH_LONG).show()
        }
        if(job!!.status == "IN_PROGRESS") {
            AsyncTaskPutHandleJson().execute(jobId.toString(), number.toString(), "FINISHED")
            Toast.makeText(baseContext, "Jobs is finished!", Toast.LENGTH_LONG).show()
        }
        val urlGet = "http://10.0.2.2:8080/drivers/8/jobs"
        AsyncTaskGetHandleJson().execute(urlGet)
    }

    inner class AsyncTaskPutHandleJson: AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String? {

            val urlPut = "http://10.0.2.2:8080/jobs/"
            var text: String
            val connection = URL(urlPut+ params[0]).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.doOutput =true
            connection.doInput = true
            connection.useCaches = false
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            try {
                connection.connect()
                val out = OutputStreamWriter(connection.outputStream)
                var jobDto = jobDtoList.find { it.number.toString() == params[1] }
                jobDto!!.status = params[2]!!
                val json = Gson().toJson(jobDto)
                out.write(json.toString())
                out.flush()
                out.close()
            }finally {
                val stream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(stream))
                text = reader.readLine()
                connection.disconnect()
            }

            return text
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            AsyncTaskGetHandleJson()
        }
    }
}

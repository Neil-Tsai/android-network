package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.network.TestApi
import com.google.gson.JsonObject
import com.example.myapplication.rxUtil.executeResult
import com.example.myapplication.rxUtil.ResultObserver
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.base1.setOnClickListener {
            MainScope().launch {
                try {
                    val jsonData = TestApi.getServerConfig()
                    Toast.makeText(this@MainActivity, "$jsonData", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("error1", e.toString())
                }
            }
        }

        binding.base2.setOnClickListener {
            MainScope().launch {
                try {
                    val jsonData = TestApi.getServerConfig2()
                    Toast.makeText(this@MainActivity, "$jsonData", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("error2", e.toString())
                }

            }
        }

        binding.new1.setOnClickListener {
            MainScope().launch {
                TestApi.getConfig().executeResult(object : ResultObserver<JsonObject>() {
                    override fun onRequestStart() {
                        //TODO start loading
                    }

                    override fun onRequestEnd() {
                        //TODO finish loading
                    }

                    override fun onSuccess(result: JsonObject?) {
                        Toast.makeText(this@MainActivity, "$result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Throwable, isNetWorkError: Boolean) {
                        Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.new2.setOnClickListener {
            MainScope().launch {
                TestApi.getConfig2().executeResult(object : ResultObserver<JsonObject>() {
                    override fun onRequestStart() {
                        //TODO start loading
                    }

                    override fun onRequestEnd() {
                        //TODO finish loading
                    }

                    override fun onSuccess(result: JsonObject?) {
                        Toast.makeText(this@MainActivity, "$result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Throwable, isNetWorkError: Boolean) {
                        Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

    }
}
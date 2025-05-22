package com.example.thebreak

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BreaksActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_breaks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            btnAnim(this@BreaksActivity, backBtn)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<RecyclerView>(R.id.breaksContainer).apply{
            layoutManager = LinearLayoutManager(this@BreaksActivity)
            adapter = BreakAdapter(getAvailableBreaks())
        }
    }

    private fun getAvailableBreaks(): List<Break> {
        val names = Breaks.breakName
        val descriptions = Breaks.breakDescription
        val images = Breaks.breakImage

        return BreakId.entries.mapNotNull { id ->
            if (containsId(id, names, descriptions, images)) {
                Break(
                    name = names[id]!!,
                    description = descriptions[id]!!,
                    image = images[id]!!
                )
            } else null
        }
    }

    private fun containsId(breakID: BreakId, vararg maps: Map<BreakId, Any>): Boolean {
        maps.forEach {
            if (breakID !in it.keys) { return false }
        }
        return true
    }
}
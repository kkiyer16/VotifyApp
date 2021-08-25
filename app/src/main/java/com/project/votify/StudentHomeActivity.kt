package com.project.votify

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.project.votify.adapter.PageAdapter
import com.project.votify.adapter.ZoomOutPageTransformer
import com.project.votify.databinding.ActivityStudentHomeBinding

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityStudentHomeBinding
    lateinit var adapter : PageAdapter
    var prevMenuItem: MenuItem? = null
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var sharedPreferences: SharedPreferences
    lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setDrawer(null)
        dbRef = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences(packageName, 0)
        adapter = PageAdapter(supportFragmentManager)
        binding.bottomNavigation.setOnNavigationItemSelectedListener {item->
            when(item.itemId){
                R.id.home-> binding.fragmentContainer.currentItem = 0
                R.id.polls-> binding.fragmentContainer.currentItem = 1
                R.id.profile-> binding.fragmentContainer.currentItem = 2
            }
            false
        }

        binding.fragmentContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null){
                    prevMenuItem!!.isChecked = false
                }
                else{
                    binding.bottomNavigation.menu.getItem(0).isChecked = false
                }
                binding.bottomNavigation.menu.getItem(position).isChecked = true
                prevMenuItem = binding.bottomNavigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        setUpPager()
    }

    fun setDrawer(toolbar: Toolbar?) {
        toggle = ActionBarDrawerToggle(this, binding.drawerLayoutStudentHome, toolbar, R.string.open, R.string.close)
        binding.drawerLayoutStudentHome.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
        binding.drawerNavBar.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.drawer_home->{
                    binding.fragmentContainer.currentItem = 0
                }
                R.id.drawer_polls->{
                    binding.fragmentContainer.currentItem = 1
                }
                R.id.drawer_results->{
                    startActivity(Intent(applicationContext, ViewResultActivity::class.java))
                }
                R.id.drawer_add_post->{
                    startActivity(Intent(applicationContext, AddPostActivity::class.java))
                }
                R.id.drawer_profile->{
                    binding.fragmentContainer.currentItem = 2
                }
                R.id.drawer_logout->{
                    AlertDialog.Builder(this).apply {
                        setTitle("Are you sure?")
                        setPositiveButton("OK"){ _, _ ->
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(applicationContext, "Logged Out!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, SignInActivity::class.java))
                            finish()
                        }
                        setNegativeButton("Cancel"){ _, _ ->
                        }
                    }.create().show()
                }
            }
            binding.drawerLayoutStudentHome.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun opendrawer() {
        binding.drawerLayoutStudentHome.openDrawer(GravityCompat.START)
    }

    private fun setUpPager() {
        val homeFragment = HomeFragment()
        val pollsFragment = PollsFragment()
        val profileFragment = ProfileFragment()
        adapter.addFrag(homeFragment)
        adapter.addFrag(pollsFragment)
        adapter.addFrag(profileFragment)
        binding.fragmentContainer.adapter = adapter
        binding.fragmentContainer.setPageTransformer(true, ZoomOutPageTransformer())
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeaderDetails()
        loadUserDetails()
    }

    private fun loadUserDetails() {
        dbRef.child("Votify").child("Users").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val email = snapshot.child("emailid").value.toString()
                            val name = snapshot.child("name").value.toString()
                            val proimg = snapshot.child("profileimageurl").value.toString()
                            sharedPreferences.edit().apply {
                                putString("emailid", email)
                                putString("name", name)
                                putString("profileimageurl", proimg)
                            }.apply()
                            binding.drawerNavBar.getHeaderView(0).findViewById<TextView>(R.id.navuseremail).text = email
                            binding.drawerNavBar.getHeaderView(0).findViewById<TextView>(R.id.navusername).text = name
                            Glide.with(applicationContext).load(proimg).into(binding.drawerNavBar.getHeaderView(0).findViewById<CircularImageView>(R.id.profileimg))
                        }
                    }
                }
            )
    }

    private fun updateHeaderDetails() {
        if (sharedPreferences.contains("emailid")) {
            if (sharedPreferences.getString("emailid", "") == FirebaseAuth.getInstance().currentUser?.email.toString()) {
                val header = binding.drawerNavBar.getHeaderView(0)
                header.findViewById<TextView>(R.id.navusername).text = sharedPreferences.getString("name", "").toString()
                header.findViewById<TextView>(R.id.navuseremail).text = sharedPreferences.getString("emailid", "").toString()
                val proimg = header.findViewById<CircularImageView>(R.id.profileimg)
                Glide.with(this).load(sharedPreferences.getString("profileimageurl", "").toString()).into(proimg)
            }
        }
    }
}
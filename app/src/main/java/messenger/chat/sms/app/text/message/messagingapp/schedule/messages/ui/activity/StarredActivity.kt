package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Sort
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityStarredBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.StarredAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.anyOf
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class StarredActivity : BaseActivity<ActivityStarredBinding>() {
    override fun getViewBinding() = ActivityStarredBinding.inflate(layoutInflater)

    private lateinit var starredAdapter: StarredAdapter
    private lateinit var conversation: ConversationNew
    private var conversationList = arrayListOf<ConversationNew>()

    override fun initData() {
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        manageMarquee()
        setUpToolBar()
        viewClick()
        setupAdapter()
    }

    override fun onResume() {
        super.onResume()
        observeLiveData()
    }

    private fun manageMarquee() {}

    private fun observeLiveData() {
        getRealmThread { realm ->
            conversationList.clear()
            conversationList = arrayListOf()
            val messages = realm.where(MessageNew::class.java)
                .sort("date", Sort.DESCENDING)
                .equalTo("isStarred", true).findAll().map { realm.copyFromRealm(it) }

            if(messages.isEmpty()){
                conversationList = arrayListOf()
                binding.tvNoData.visible()
                starredAdapter.submitList(arrayListOf())
            }else {
                messages.forEach { msg ->
                    val conversation = realm.where(ConversationNew::class.java)
                        .anyOf("id", listOf(msg.threadId).toLongArray())
                        .findFirst()?.let { it1 -> realm.copyFromRealm(it1) }

                    conversation?.let {
                        it.lastMessage = msg
                        conversationList.add(it)
                    }
                }

                if (conversationList.isEmpty()) {
                    binding.tvNoData.visible()
                } else {
                    binding.tvNoData.gone()
                    starredAdapter.submitList(conversationList)
                }
            }
        }
    }

    private fun viewClick() {
        binding.ivScrollUp.setOnClickListener(1000L) {
            scrollToTop()
            binding.ivScrollUp.gone()
        }
    }


    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = getString(R.string.starred)
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAdapter() {
        binding.rvStarred.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        starredAdapter = StarredAdapter(
            this, myPreferences, PhoneNumberUtils(this)
        ) { item, unStarredEvent ->

            if (unStarredEvent) {
                conversation = item!!
                unStarredMessages(item.lastMessage!!)
            } else {
                if (item != null) {
                    conversation = item
                    startActivity(
                        Intent(
                            this,
                            ConversationDetailActivityNew::class.java
                        ).putExtra(CommonClass.THREAD_ID, item.id)
                            .putExtra(CommonClass.THREAD_TITLE, item.getTitle())
                    )
                }
            }
        }
        binding.rvStarred.adapter = starredAdapter
        val scrollListener = MyScrollListener(object : OnScrollListener {
            override fun onScrolledUp() {}

            override fun onScrolledDown() {
                binding.ivScrollUp.visible()
            }

            override fun onReachedBottom() {}

            override fun onReachedTop() {
                binding.ivScrollUp.gone()
            }
        })
        binding.rvStarred.addOnScrollListener(scrollListener)
    }

    private fun unStarredMessages(item: MessageNew) {
        val list = arrayListOf<Long>()
        list.add(item.id)
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("id", list.toLongArray())
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.isStarred = false
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                observeLiveData()
            }, 300)
        }


//        CoroutineScope(Dispatchers.IO).launch {
//            item.apply {
//                viewModel.unStarredMessageById(id)
//            }
//        }
    }

    private fun scrollToTop() {
        val ll = binding.rvStarred.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(0, 0)
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()
        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
    }

    override fun onBackPressed() {
        backPressed()
    }

}
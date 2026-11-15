package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.core.AmbientMusicPlayer
import com.example.core.BillingAndAdProvider
import com.example.core.ConnectivityObserver
import com.example.core.NotificationDispatcher
import com.example.core.StudyQuestPreferences
import com.example.data.local.StudyQuestDatabase
import com.example.data.repository.AcademicProgressRepository
import com.example.data.repository.AdminStudioRepository
import com.example.data.repository.AiDoubtSolverRepository
import com.example.data.repository.AiVideoRepository
import com.example.data.repository.AuthAndProfileRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.CompetitionRepository
import com.example.data.repository.FocusSessionRepository
import com.example.data.repository.GamificationEngine
import com.example.data.repository.GuestDataRepository
import com.example.data.repository.SocialRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object StudyQuestViewModelFactories {

    private fun CreationExtras.requireApp(): Application =
        checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) {
            "APPLICATION_KEY missing from CreationExtras"
        }

    private fun firestoreInstance(app: Application): FirebaseFirestore {
        val dbId = app.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(dbId)
    }

    private fun buildGamificationEngine(app: Application, db: FirebaseFirestore): GamificationEngine {
        val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
        val prefs = StudyQuestPreferences(app)
        val notifications = NotificationDispatcher(db, prefs, app)
        return GamificationEngine(db, dao, prefs, notifications)
    }

    fun preAuthFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            PreAuthViewModel(StudyQuestPreferences(app))
        }
    }

    fun guestSessionFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val prefs = StudyQuestPreferences(app)
            val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
            val db = firestoreInstance(app)
            GuestSessionViewModel(
                preferences = prefs,
                guestRepo = GuestDataRepository(dao, prefs),
                aiRepo = AiDoubtSolverRepository(db),
                connectivityObserver = ConnectivityObserver(app),
                gamificationEngine = buildGamificationEngine(app, db)
            )
        }
    }

    fun mainSessionFactory(user: FirebaseUser): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
            MainSessionViewModel(
                currentUser = user,
                authRepo = AuthAndProfileRepository(db, dao),
                adminRepo = AdminStudioRepository(db),
                preferences = StudyQuestPreferences(app),
                connectivityObserver = ConnectivityObserver(app),
                appContext = app,
                gamificationEngine = buildGamificationEngine(app, db),
                billingProvider = BillingAndAdProvider(db)
            )
        }
    }

    fun learnAndExamFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
            val prefs = StudyQuestPreferences(app)
            val guestRepo = GuestDataRepository(dao, prefs)
            val engine = buildGamificationEngine(app, db)
            LearnAndExamViewModel(
                currentUserId = userId,
                academicRepo = AcademicProgressRepository(db, dao, app, prefs, guestRepo, engine),
                aiRepo = AiDoubtSolverRepository(db)
            )
        }
    }

    fun competeFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val engine = buildGamificationEngine(app, db)
            CompeteViewModel(
                currentUserId = userId,
                compRepo = CompetitionRepository(db, engine)
            )
        }
    }

    fun socialFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
            val prefs = StudyQuestPreferences(app)
            val notifications = NotificationDispatcher(db, prefs, app)
            val engine = GamificationEngine(db, dao, prefs, notifications)
            SocialViewModel(
                currentUserId = userId,
                socialRepo = SocialRepository(db),
                authRepo = AuthAndProfileRepository(db, dao),
                communityRepo = CommunityRepository(db, engine, notifications)
            )
        }
    }

    fun adminStudioFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            AdminStudioViewModel(
                currentUserId = userId,
                adminRepo = AdminStudioRepository(db)
            )
        }
    }

    fun focusTimerFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val dao = StudyQuestDatabase.getInstance(app).studyQuestDao()
            val prefs = StudyQuestPreferences(app)
            val notifications = NotificationDispatcher(db, prefs, app)
            val engine = GamificationEngine(db, dao, prefs, notifications)
            val focusRepo = FocusSessionRepository(db, dao, prefs, engine, notifications)
            FocusTimerViewModel(
                currentUserId = userId,
                focusRepo = focusRepo,
                preferences = prefs,
                musicPlayer = AmbientMusicPlayer()
            )
        }
    }

    fun aiVideoFactory(userId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = requireApp()
            val db = firestoreInstance(app)
            val engine = buildGamificationEngine(app, db)
            val aiVideoRepo = AiVideoRepository(db, engine)
            AiVideoViewModel(
                appContext = app,
                currentUserId = userId,
                aiVideoRepo = aiVideoRepo
            )
        }
    }
}

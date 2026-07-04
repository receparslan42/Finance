package com.receparslan.finance

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Annotate the Application class to trigger Hilt's code generation for dependency injection setup
@HiltAndroidApp
class FinanceApplication : Application()
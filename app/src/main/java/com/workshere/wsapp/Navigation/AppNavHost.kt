package com.workshere.wsapp.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.workshere.wsapp.ui.theme.Screens.Bossscreen.Bossscreen
import com.workshere.wsapp.ui.theme.Screens.Choicescreen.Choicescreen
import com.workshere.wsapp.ui.theme.Screens.Loginbossscreen.Loginboss
import com.workshere.wsapp.ui.theme.Screens.Signupbossscreen.Signupboss
import com.workshere.wsapp.ui.theme.Screens.Signupworkerscreen.Signupworker
import com.workshere.wsapp.ui.theme.Screens.Splashscreen.Splashscreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier,
               navController: NavHostController = rememberNavController(),
               startDestination: String=ROUTE_SPLASH) {
    NavHost(modifier=modifier, navController=navController,
        startDestination=startDestination){
        composable(ROUTE_SPLASH){
            Splashscreen(navController)
        }
        composable(ROUTE_CHOICE){
            Choicescreen(navController)
        }
        composable(ROUTE_SIGNUP1){
            Signupboss(navController)
        }
        composable(ROUTE_SIGNUP2){
            Signupworker(navController)
        }
        composable(ROUTE_LOGIN1){
            Loginboss(navController)
        }
        composable(ROUTE_BOSS){
            Bossscreen(navController)
        }

    }

}


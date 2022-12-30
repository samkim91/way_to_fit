import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_native_splash/flutter_native_splash.dart';

import 'firebase_options.dart';
import 'src/injector.dart';
import 'src/presentation/views/auth_view.dart';
import 'src/presentation/views/main_view.dart';

Future<void> main() async {
  WidgetsBinding widgetsBinding = WidgetsFlutterBinding.ensureInitialized();
  FlutterNativeSplash.preserve(widgetsBinding: widgetsBinding);

  await initializeDependencies();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  FlutterNativeSplash.remove();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    Widget home;

    return FutureBuilder(
        future: Future.value(FirebaseAuth.instance.currentUser),
        builder: (BuildContext context, AsyncSnapshot snapshot) {
          if (snapshot.hasData) {
            User user = snapshot.data;
            home = const MainView();
          } else {
            home = const AuthView();
          }

          return MaterialApp(
              home: home, theme: ThemeData(), darkTheme: ThemeData.dark());
        });
  }
}

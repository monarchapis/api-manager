# !/bin/sh
java -jar antlr-4.3-complete.jar -package com.monarchapis.apimanager.analytics.grammar -listener -visitor -o ../java/com/monarchapis/apimanager/analytics/grammar EventQuery.g4

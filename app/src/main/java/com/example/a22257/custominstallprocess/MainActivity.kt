package com.example.a22257.custominstallprocess

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.util.Log
import android.util.Xml
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parserFactory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        parserFactory.isNamespaceAware = true
        val xmlPullParser: XmlPullParser = parserFactory.newPullParser()
        xmlPullParser.setInput(StringReader("<foo>Hello World!</foo>"))
        val childAttrs = Xml.asAttributeSet(xmlPullParser)
        var eventType: Int = xmlPullParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> System.out.println("Start document")
                XmlPullParser.END_DOCUMENT -> System.out.println("End document")
                XmlPullParser.START_TAG -> System.out.println("Start tag " + xmlPullParser.getName())
                XmlPullParser.END_TAG -> System.out.println("End tag " + xmlPullParser.getName())
                XmlPullParser.TEXT -> System.out.println("Text " + xmlPullParser.getText())
            }
            eventType = xmlPullParser.next()
        }
    }

    override fun onResume() {
        super.onResume()
        // /data/data/com.example.a22257.custominstallprocess/cache/CloudMusic_official_5.6.0.314967.apk
        // com.example.a22257.custominstallprocess.fileprovider
//        val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
        val install = Intent(Intent.ACTION_SEND)
        val apkFile = File(this.cacheDir.path + "/CloudMusic_official_5.6.0.314967.apk")

        to_install.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                val contentUri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
//                install.setDataAndType(
//                    contentUri,
//                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//                        MimeTypeMap.getFileExtensionFromUrl(
//                            Uri.fromFile(apkFile).toString()
//                        )
//                    )
//                )
                install.putExtra(Intent.EXTRA_STREAM, contentUri)
                install.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(
                        contentUri.toString()
                    )
                )
                startActivity(install)
//                Log.e("message", "11" + install.action)
//                Log.e("message", "11" + install.data.toString())
//                Log.e("message", "11" + install.scheme)
//                Log.e("message", "11" + install.type)

//                Log.e("message", "11"+install.categories.toString())
//                Log.e("message", "11"+install.clipData.toString())
//                Log.e("message", "11"+install.component.toString())
//                Log.e("message", "11"+install.extras.toString())
//                Log.e("message", install.flags == Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
//                Log.e("message", "11"+install.selector.toString())
            } else {
                install.setDataAndType(
                    Uri.fromFile(apkFile),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(apkFile).toString())
                    )
                )
                startActivity(install)
            }
        }
    }
}

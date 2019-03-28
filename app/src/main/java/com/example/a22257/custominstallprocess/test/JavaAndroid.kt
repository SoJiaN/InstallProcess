package com.example.a22257.custominstallprocess.test

class JavaAndroid {
    fun main(args: Array<String>) {
        val annotationValue : AnnotationedClassObject = AnnotationedClassObject()
        val annotationClass =  annotationValue::class.java
        annotationClass.declaredFields.forEach { field ->
            val annotationFieldInAnnotationValue: androidAnnotation = field.getAnnotation(androidAnnotation::class.java)
            if (annotationFieldInAnnotationValue != null){

            }
        }
    }
}
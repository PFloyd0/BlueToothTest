package com.example.bluetoothtest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


//import org.dmg.pmml.FieldName;
import org.dmg.pmml.Lag;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;

public class LocationService{

    private final static  String MODEL_PATH_X = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Model/"+"knn_x.ser";
    private final static  String MODEL_PATH_Y = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Model/"+"knn_y.ser";
    private Evaluator evaluator_x;
    private Evaluator evaluator_y;

    public LocationService(){
        this.evaluator_x = loadModel(MODEL_PATH_X);
        this.evaluator_y = loadModel(MODEL_PATH_Y);
    }


    private static Evaluator loadModel(String model_path){
        Log.d("PATH**********", model_path);



        PMML pmml = new PMML(); //定义PMML对象
        Log.d(model_path, "Enter!!!!!!!!!!");
        InputStream inputStream; //定义输入流
        try {
            FileInputStream fileIn = new FileInputStream(model_path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            pmml = (PMML) in.readObject();
            in.close();
            fileIn.close();
        }catch (Exception e){
            Log.d(model_path, "Problem!!!!!");
            e.printStackTrace();
        }
        Log.d(model_path, "Success!!!!!!!!!!");
        ModelEvaluatorBuilder ev = new ModelEvaluatorBuilder(pmml);
        Evaluator evaluator = ev.build();
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance(); //实例化一个模型构造工厂
//        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml); //将PMML对象构造为Evaluator模型对象

        return evaluator;
//        PMML pmml = new PMML(); //定义PMML对象
//        InputStream inputStream; //定义输入流
//        try {
//            inputStream = new FileInputStream(model_path); //输入流接到磁盘上的模型文件
//            pmml = PMMLUtil.unmarshal(inputStream); //将输入流解析为PMML对象
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance(); //实例化一个模型构造工厂
//        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml); //将PMML对象构造为Evaluator模型对象
//
//        return evaluator;
    }

    public double[] predict(double x1, double x2, double x3, double x4){
        Map<String, Double> data = new HashMap<>(); //定义测试数据Map，存入各元自变量
        data.put("x1", x1);
        data.put("x2", x2);
        data.put("x3", x3);
        data.put("x4", x4);
        //键"x"为自变量的名称，应与训练数据中的自变量名称一致
        List<InputField> inputFieldList = evaluator_x.getInputFields(); //得到模型各元自变量的属性列表

        Map<String, FieldValue> arguments = new LinkedHashMap<String, FieldValue>();
        for (InputField inputField : inputFieldList) { //遍历各元自变量的属性列表
            String inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName); //取出该元变量的值
            FieldValue inputFieldValue = inputField.prepare(rawValue); //将值加入该元自变量属性中
            arguments.put(inputFieldName, inputFieldValue); //变量名和变量值的对加入LinkedHashMap
        }

        Map<String, ?> results_x = evaluator_x.evaluate(arguments);
        Map<String, ?> results_y = evaluator_y.evaluate(arguments);//进行预测
        List<TargetField> targetFieldList = evaluator_x.getTargetFields(); //得到模型各元因变量的属性列表
        String targetFieldName = targetFieldList.get(0).getName();
//        System.out.println(targetFieldList);//第一元因变量名称
//        System.out.println(targetFieldName);
//        System.out.println(results);
        Object targetFieldValue_x = results_x.get(targetFieldName);
        Object targetFieldValue_y = results_y.get(targetFieldName);//由因变量名称得到值


        return resolve(targetFieldValue_x, targetFieldValue_y);
    }
    private static double[] resolve(Object x, Object y){
        String line_x = x.toString();
        String line_y = y.toString();
        String pattern = "result=(\\d+.\\d+)";

        Pattern pa = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m_x = pa.matcher(line_x);
        Matcher m_y = pa.matcher(line_y);
        String temp_x = "";
        String temp_y = "";
        if (m_x.find( ) && m_x.find( )) {
            temp_x = m_x.group(1);
            temp_y = m_y.group(1);
            System.out.println("RESULT: " + temp_x );
        } else {
            System.out.println("NO RESULT");
        }
        return new double[]{Double.parseDouble(temp_x), Double.parseDouble(temp_y)};
    }
}
package com.example.bluetoothtest;

import android.os.Environment;

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

//import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.JAXBException;


//import org.jpmml.evaluator.ValueFactoryFactory;
//import org.jpmml.evaluator.testing.CsvUtil;
//import org.jpmml.evaluator.visitors.AttributeFinalizerBattery;
//import org.jpmml.evaluator.visitors.AttributeInternerBattery;
//import org.jpmml.evaluator.visitors.AttributeOptimizerBattery;
//import org.jpmml.evaluator.visitors.ElementFinalizerBattery;
//import org.jpmml.evaluator.visitors.ElementInternerBattery;
//import org.jpmml.evaluator.visitors.ElementOptimizerBattery;
//import org.jpmml.model.visitors.LocatorNullifier;
//import org.jpmml.model.visitors.MemoryMeasurer;
//import org.jpmml.model.visitors.VisitorBattery;


public class Location {
    public static void main(String[] args) throws JAXBException, IOException, ParserConfigurationException, SAXException {

        String model_path = "C:\\Users\\changwei.li\\Downloads\\BluetoothTest\\app\\src\\main\\java\\com\\example\\bluetoothtest\\knn_x.ser"; //模型路径





        double[][] x = {{-55.68949725, -58.60409405, -50.70743288, -55.09241215}}; //测试的自变量值
        Evaluator model = loadModel(model_path); //加载模型
        double x1 = -55.68949725;
        double x2 = -58.60409405;
        double x3 = -50.70743288;
        double x4 = -55.09241215;


        System.out.println("预测的结果为:" + model.getInputFields());
        Object r = predict(model, x1, x2, x3, x4); //预测
        String line = r.toString();
        String pattern = "result=(\\d+.\\d+)";

        Pattern pa = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = pa.matcher(line);
        String temp = "";
        if (m.find( )) {
            temp = m.group(1);
            System.out.println("RESULT: " + temp );
        } else {
            System.out.println("NO RESULT");
        }
        Double result = Double.parseDouble(temp);
        System.out.println("预测的结果为:" + result);
    }

    private static Evaluator loadModel(String model_path){
        PMML pmml = new PMML(); //定义PMML对象
        InputStream inputStream; //定义输入流
        try {
            FileInputStream fileIn = new FileInputStream(model_path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            pmml = (PMML) in.readObject();
            in.close();

            fileIn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        ModelEvaluatorBuilder ev = new ModelEvaluatorBuilder(pmml);
        Evaluator evaluator = ev.build();
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance(); //实例化一个模型构造工厂
//        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml); //将PMML对象构造为Evaluator模型对象

        return evaluator;

    }

    private static Object predict(Evaluator evaluator, double x1, double x2, double x3, double x4){
        Map<String, Double> data = new HashMap<>(); //定义测试数据Map，存入各元自变量
        data.put("x1", x1);
        data.put("x2", x2);
        data.put("x3", x3);
        data.put("x4", x4);
        //键"x"为自变量的名称，应与训练数据中的自变量名称一致
        List<InputField> inputFieldList = evaluator.getInputFields(); //得到模型各元自变量的属性列表

        Map<String, FieldValue> arguments = new LinkedHashMap<String, FieldValue>();
        for (InputField inputField : inputFieldList) { //遍历各元自变量的属性列表
            String inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName); //取出该元变量的值
            FieldValue inputFieldValue = inputField.prepare(rawValue); //将值加入该元自变量属性中
            arguments.put(inputFieldName, inputFieldValue); //变量名和变量值的对加入LinkedHashMap
        }

        Map<String, ?> results = evaluator.evaluate(arguments); //进行预测
        List<TargetField> targetFieldList = evaluator.getTargetFields(); //得到模型各元因变量的属性列表
        String targetFieldName = targetFieldList.get(0).getName();
        System.out.println(targetFieldList);//第一元因变量名称
        System.out.println(targetFieldName);
        System.out.println(results);
        Object targetFieldValue = results.get(targetFieldName); //由因变量名称得到值


        return targetFieldValue;
    }

}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.finalproject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;

/**
 *
 * @author brito
 */
//@Named(value = "finalBean")
//@ApplicationScoped
@ManagedBean
public class finalBean {

    private final static String QUEUE_NAME = "hello";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private Consumer consumer;
    private LineChartModel chart;
    private ChartSeries cs = new ChartSeries();

    /**
     * Creates a new instance of finalBean
     */
    public finalBean(LineChartModel chart) throws IOException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        this.chart = chart;
        cs.setLabel("Heart Rate");
        initConsumer();
    }

    private void initConsumer() throws IOException {
        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                setValue(message);
                setCView(message);
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
    private String value;

    public void setValue(String v) {
        value = v;
        float temp = Float.parseFloat(value);
        if (temp > 150) {
            Constants.incrementAlarmHigh();
        }

        if (temp < 90 && temp != -1) {
            Constants.incrementAlarmLow();
        }
    }

    public String getValue() {
        return value;
    }

    public void setCView(String value) {
        SharedChart.updateChart(Math.round(Float.parseFloat(value)));
    }

    public LineChartModel getCView() {
        return SharedChart.getChart();
    }
}

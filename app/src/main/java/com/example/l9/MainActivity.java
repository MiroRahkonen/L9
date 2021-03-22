package com.example.l9;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity{
    ArrayList<String> theater_list = new ArrayList<>();
    Spinner spinner;
    TextView text;
    TextView movie_selection;
    Button button;
    LinearLayout linear_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        readtheater_XML();
        movie_selection = (TextView) findViewById(R.id.movie_selection);
        spinner = (Spinner) findViewById(R.id.spinner);
        button = (Button) findViewById(R.id.button);
        linear_layout = (LinearLayout) findViewById(R.id.linear_layout);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, theater_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        button_functionality();
    }

    public void button_functionality(){ /*Pitää sisällään 'select location' napin toiminnallisuuden*/
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String theater_location = (String) spinner.getSelectedItem();
                create_scrollview(theater_location);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void create_scrollview(String theater_location){
        String[] items = theater_location.split("/");
        String theaterID = items[0];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);
        String url = "https://www.finnkino.fi/xml/Schedule/?area="+theaterID+"&dt="+date;
        System.out.println(url);
        readmovie_XML(url);
    }

    public void readtheater_XML(){ /*Lisää ScrollViewiin saatavilla olevat teatterit*/
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String urlString = "https://www.finnkino.fi/xml/TheatreAreas/";
            Document document = builder.parse(urlString);
            document.getDocumentElement().normalize();
            System.out.println("Root element: "+document.getDocumentElement().getNodeName());

            NodeList nList = document.getDocumentElement().getElementsByTagName("TheatreArea");

            for(int i=0; i < nList.getLength();i++){
                Node node = nList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element) node;
                    String city = (String) element.getElementsByTagName("Name").item(0).getTextContent();
                    String ID = (String) element.getElementsByTagName("ID").item(0).getTextContent();
                    String place = ID+"/"+city;
                    theater_list.add(place);
                }
            }
            theater_list.remove(0); //Poistetaan tarpeettomat otsikkotiedot
            theater_list.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally{
            System.out.println("//DONE//");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void readmovie_XML(String url){ /*Lisää linear layouttiin samana päivänä olevat elokuvat */
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(url);
            document.getDocumentElement().normalize();
            System.out.println("Root element: "+document.getDocumentElement().getNodeName());
            NodeList nList = document.getDocumentElement().getElementsByTagName("Show");

            for(int i=0; i < nList.getLength() ;i++){
                Node node = nList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element) node;

                    String title = (String) element.getElementsByTagName("Title").item(0).getTextContent();     /*Haetaan elokuvan tietoja*/
                    String time_start = (String) element.getElementsByTagName("dttmShowStart").item(0).getTextContent();
                    String time_end = (String) element.getElementsByTagName("dttmShowEnd").item(0).getTextContent();
                    String auditorium = (String) element.getElementsByTagName("TheatreAuditorium").item(0).getTextContent();
                    DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern("HH.mm");
                    DateTimeFormatter dtf_date = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDateTime parsed_start = LocalDateTime.parse(time_start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    LocalDateTime parsed_end = LocalDateTime.parse(time_end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String ftime_start = parsed_start.format(dtf_time);
                    String ftime_end = parsed_end.format(dtf_time);
                    String fdate = parsed_start.format(dtf_date);

                    String movie_info = String.format("%s \n(%s) %s - %s\nAuditorium: %s",title,fdate,ftime_start,ftime_end,auditorium);

                    TextView temp_text = new TextView(this);
                    temp_text = edit_textview(temp_text,movie_info);
                    linear_layout.addView(temp_text);
                    Space space = new Space(this);  /*Lisätään väli listan alkioiden välille*/
                    space.setMinimumHeight(40);
                    linear_layout.addView(space);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally{
            System.out.println("//DONE//");
        }

    }
    public TextView edit_textview(TextView temp,String info){ /*Muokataan elokuvien teskialkioita linear layoutissa*/
        temp.setTextIsSelectable(true);
        temp.setText(info);
        temp.setTextSize(18);
        temp.setGravity(Gravity.CENTER);
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movie_selection.setText(info);
            }
        });
        return temp;
    }
}
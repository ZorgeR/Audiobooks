package com.zlab.audiobooks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AudiobooksMain extends FragmentActivity {

	public static AudiobooksMain mainContext = null;
	public static String SettingsThemeSelected;
	public static boolean buildinplayer;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    
    // Прочее
	public static String handlMessage;
    public Runnable updateLib;
    public ProgressDialog m_ProgressParseXML = null;
    public ProgressDialog m_ProgressLoadXML = null;
    static int len=0;
    static int render_main=0;
    static int render_classic=0;
    static int render_modern=0;
    static int render_fantastic=0;
    boolean download_complete = false;
    boolean parse_complete = false;
    boolean access_complete = false;
    // URI
	static File root = android.os.Environment.getExternalStorageDirectory();
	static File dir  = new File (root.getAbsolutePath() + "/audiobooks/xmls");
	static File file = new File(dir, "library.xml");
    // Переменные библиотеки
    static String LibNAME="undef";
    static String LibDiscription;
    static String LibLogoURL;
    static String LibBookNumber;
    static String LibBookStorageKB;
    static String LibType;
    static String LibLastUpdate;
    static String LibXML;
    static String LibWebURL;
    // Переменные книг
    static String[] GenreCODE;
    static String[] LanguageCODE;
    static String[] SizeKB;
    static String[] BookName;
    static String[] BookLogoURL;
    static String[] ReleaseDATA;
    static String[] MediaURL;
    static String[] Author;
    // Хранение данных
    static Integer[] GenreIDClassic;
    static Integer[] GenreIDModern;
    static Integer[] GenreIDFantastic;
    static int GenreLenClassic;
    static int GenreLenModern;
    static int GenreLenFantastic;
    // TextView
    TextView txtLibrary_Name;
	TextView txtDiscription;
	TextView txtBookTotal;
	TextView txtBookXML;
	TextView txtStorageSize;
	TextView txtType;
	TextView txtLastUpdate;
	TextView txtXMLUrl;
	TextView txtLibUrl;
	//public SharedPreferences xml_store;
	//public SharedPreferences.Editor xml_store_editor;
    // Списки
    ListView ListOfTab;
    ListView ListOfTabClassic;
    ListView ListOfTabModern;
    ListView ListOfTabFantastic;
    RelativeLayout MainPage;
    RelativeLayout updatePage;
    ListElements[] list_tab_classic = new ListElements[]
	        {new ListElements(R.drawable.book_256, "Нет книг этого жанра!", "", "", "")};
    ListElements[] list_tab_modern = new ListElements[]
	        {new ListElements(R.drawable.book_256, "Нет книг этого жанра!", "", "", "")};
    ListElements[] list_tab_fantastic = new ListElements[]
	        {new ListElements(R.drawable.book_256, "Нет книг этого жанра!", "", "", "")};
    
    
    // ----------- //
    // Обработчики //
    Handler err = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast = Toast.makeText(getApplicationContext(), handlMessage, Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.err);
            ToastView.addView(imageWorld, 0);
            toast.show();
        }
    };
    
    Handler noerr = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast = Toast.makeText(getApplicationContext(), handlMessage, Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.good);
            ToastView.addView(imageWorld, 0);
            toast.show();
        }
    };
    
    Handler updateAdaptor = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSectionsPagerAdapter.startUpdate(mViewPager);
        }
    };
    
    Handler reloadHandler = new Handler() {
    	public void handleMessage(Message msg) {
            super.handleMessage(msg);
            reload();
        }
    };
    Handler showParseXML = new Handler() {
    	public void handleMessage(Message msg) {
            super.handleMessage(msg);
            m_ProgressParseXML = ProgressDialog.show(com.zlab.audiobooks.AudiobooksMain.mainContext, "Подождите ...", "Идет разбор XML ...", true);
        }
    };
    Handler showLoadXML = new Handler() {
    	public void handleMessage(Message msg) {
            super.handleMessage(msg);
            m_ProgressLoadXML = ProgressDialog.show(com.zlab.audiobooks.AudiobooksMain.mainContext, "Подождите ...", "Идет загрузка XML ...", true);
        }
    };
    // Обработчики //
    // ----------- //

    
      	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    		mainContext = this;

        	// Читаем настройки
            getPrefs();
            // Применяем настройки
            setPrefs();
            // Фиксируем портретный режим
            setOrientation();

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_audiobooks_main);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            
        	// Проверяем парсен ли файл
        	if (file.exists()==true && len==0){
        		showParseXML.sendEmptyMessage(0);
        		parseinbackground();}

        	// Определяем шрифты
        	/*
            Typeface ptcaption=Typeface.createFromAsset(getAssets(),"fonts/ptcaption.ttf");
            Typeface ptcaptionnormal=Typeface.createFromAsset(getAssets(),"fonts/ptcaptionnormal.ttf");
            */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_audiobooks_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.exit:{
       	 				finish();
       	 				return true;}
        case R.id.menu_settings:{
       	 						Intent settingsActivity = new Intent(getBaseContext(),
       	 						com.zlab.audiobooks.AudiobooksSettings.class);
       	 						startActivity(settingsActivity);
       	 						return true;}
        case R.id.update_button:{
        						updateinbackground();
						return true;}
        case R.id.flush:{
        						if(file.exists()==true) {file.delete();}
						        handlMessage=getString(R.string.flush_done);
						  		noerr.sendEmptyMessage(0);
						  		reload();
						return true;}
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
                case 2: return getString(R.string.title_section3).toUpperCase();
                case 3: return getString(R.string.title_section4).toUpperCase();
            }
            return null;
        }
    }

    public class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

        	// Cписок

            Bundle args = getArguments();

        	if (Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("1")){
        		if (len!=0){
        			// View с заглавной
                	// Заглавная
                	LayoutInflater inflateMainPage = (LayoutInflater) mainContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                	MainPage = (RelativeLayout) inflateMainPage.inflate(R.layout.main_page, null);

        			// ImageView icoLibrary_Logo 	= (ImageView) MainPage.findViewById(R.id.icoLibrary_Logo);
        			txtLibrary_Name 	= (TextView) MainPage.findViewById(R.id.txtLibrary_Name);
        			txtDiscription 		= (TextView) MainPage.findViewById(R.id.txtDiscription);
        			txtBookTotal 		= (TextView) MainPage.findViewById(R.id.txtBookTotal);
        			txtBookXML 			= (TextView) MainPage.findViewById(R.id.txtBookXML);
        			txtStorageSize 		= (TextView) MainPage.findViewById(R.id.txtStorageSize);
        			txtType 			= (TextView) MainPage.findViewById(R.id.txtType);
        			txtLastUpdate		= (TextView) MainPage.findViewById(R.id.txtLastUpdate);
        			txtXMLUrl 			= (TextView) MainPage.findViewById(R.id.txtXMLUrl);
        			txtLibUrl 			= (TextView) MainPage.findViewById(R.id.txtLibUrl);

        			BigDecimal LibBookStorageMB = new BigDecimal(Integer.parseInt(LibBookStorageKB)/1024.00);
        			LibBookStorageMB = LibBookStorageMB.setScale(2, BigDecimal.ROUND_HALF_UP);
        			
        			//Library_Logo.setImageURI( LibLogoURL );
        			txtLibrary_Name.setText(	"  "+LibNAME);
        			txtDiscription.setText(		"  "+LibDiscription);
        	        txtBookTotal.setText(		"  "+LibBookNumber);
        	        txtBookXML.setText(			"  "+len);
        	        txtStorageSize.setText(		"  "+LibBookStorageMB+" MB");
        	        txtType.setText(			"  "+LibType);
        	        txtLastUpdate.setText(		"  "+LibLastUpdate);
        	        txtXMLUrl.setText(			"  "+LibXML);
        	        txtLibUrl.setText(			"  "+LibWebURL);
        	        
        	        /*
        	        // Меняем шрифт
        		    //
        		    txtLibrary_Name.setTypeface(ptcaptionnormal);
        			txtDiscription.setTypeface(ptcaption);
        	        txtBookTotal.setTypeface(ptcaption);
        	        txtBookXML.setTypeface(ptcaption);
        	        txtStorageSize.setTypeface(ptcaption);
        	        txtType.setTypeface(ptcaption);
        	        txtLastUpdate.setTypeface(ptcaption);
        	        txtXMLUrl.setTypeface(ptcaption);
        	        txtLibUrl.setTypeface(ptcaption);
        	        //
        	        txtmain0.setTypeface(ptcaptionnormal);
        	        txtmain1.setTypeface(ptcaptionnormal);
        	        txtmain2.setTypeface(ptcaptionnormal);
        	        txtmain3.setTypeface(ptcaptionnormal);
        	        txtmain4.setTypeface(ptcaptionnormal);
        	        txtmain5.setTypeface(ptcaptionnormal);
        	        txtmain6.setTypeface(ptcaptionnormal);
        	        txtmain7.setTypeface(ptcaptionnormal);
        	        txtmain8.setTypeface(ptcaptionnormal);
        	        */
        		}        	
        	} else
        	if (Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("2")){
            	if (GenreLenClassic!=0){
                	// Шапки
                	View header = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_header_row, null);
                	View footer = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_footer_row, null);

            		// Длинна списка
            		list_tab_classic = new ListElements[GenreLenClassic];
                	// Заголовок с счетчиком
                	TextView txtheader = (TextView) header.findViewById(R.id.txtHeader);
                	txtheader.setText(getString(R.string.Header)+" "+GenreLenClassic);
                	/*
                	txtheader.setTypeface(ptcaptionnormal);
                	*/
                	/*
                	BigDecimal SizeMBfloat = new BigDecimal(Integer.parseInt(SizeKB[i])/1024.00);
        			SizeMBfloat = SizeMBfloat.setScale(2, BigDecimal.ROUND_HALF_UP);*/
                	for (int i=0; i<GenreLenClassic; i++) {
                		list_tab_classic[i] = new ListElements(R.drawable.book_256, BookName[GenreIDClassic[i]], Author[GenreIDClassic[i]], "Размер:", SizeKB[GenreIDClassic[i]]+" Мб");
                	}
                	ListOfTabClassic = new ListView(getActivity());
                	ListElementsAdapter ListAdapterClassic = new ListElementsAdapter(mainContext,R.layout.listview_item_row, list_tab_classic);
                	ListOfTabClassic.addHeaderView(header);
                	ListOfTabClassic.addFooterView(footer);
                	ListOfTabClassic.setAdapter(ListAdapterClassic);
                	ListOfTabClassic.setDivider(getResources().getDrawable(R.drawable.item_divide_horizontal));
                	ListOfTabClassic.setDividerHeight(2);

                	int paddingPixel = 10;
                	float density = com.zlab.audiobooks.AudiobooksMain.mainContext.getResources().getDisplayMetrics().density;
                	int paddingDp = (int)(paddingPixel * density);
                	ListOfTabClassic.setPadding(paddingDp,0,paddingDp,0);
                	
                	ListOfTabClassic.setOnItemClickListener(new OnItemClickListener() 
                    {
        				public void onItemClick(AdapterView<?> arg0, View arg1,
        						int position, long id) {
        							// Передача переменных активити деталей
        			                Intent detail = new Intent(com.zlab.audiobooks.AudiobooksMain.mainContext, com.zlab.audiobooks.AudiobooksDetail.class);
        			                detail.putExtra("BookName",		BookName[GenreIDClassic[(int)id]]);
        			                detail.putExtra("Author", 		Author[GenreIDClassic[(int)id]]);
        			                detail.putExtra("BookLogoURL",	BookLogoURL[GenreIDClassic[(int)id]]);
        			                detail.putExtra("ReleaseDATA",	ReleaseDATA[GenreIDClassic[(int)id]]);
        			                detail.putExtra("MediaURL",		MediaURL[GenreIDClassic[(int)id]]);
        			                detail.putExtra("SizeKB",		SizeKB[GenreIDClassic[(int)id]]);
        			                detail.putExtra("LanguageCODE", LanguageCODE[GenreIDClassic[(int)id]]);
        			                detail.putExtra("BuildInPlayer", buildinplayer);
        			                startActivity(detail);
        				}
                    });
        		}
        	} else
            if (Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("3")){
            	if (GenreLenModern!=0){
                	// Шапки
                	View header = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_header_row, null);
                	View footer = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_footer_row, null);

            		// Длинна списка
            		list_tab_modern = new ListElements[GenreLenModern];
                	// Заголовок с счетчиком
                	TextView txtheader = (TextView) header.findViewById(R.id.txtHeader);
                	txtheader.setText(getString(R.string.Header)+" "+GenreLenModern);
                	/*
                	txtheader.setTypeface(ptcaptionnormal);
                	*/
                	/*
                	BigDecimal SizeMBfloat = new BigDecimal(Integer.parseInt(SizeKB[i])/1024.00);
        			SizeMBfloat = SizeMBfloat.setScale(2, BigDecimal.ROUND_HALF_UP);*/
                	for (int i=0; i<GenreLenModern; i++) {
                		list_tab_modern[i] = new ListElements(R.drawable.book_256, BookName[GenreIDModern[i]], Author[GenreIDModern[i]], "Размер:", SizeKB[GenreIDModern[i]]+" Мб");
                	}
                	ListOfTabModern = new ListView(getActivity());
                	ListElementsAdapter ListAdapterClassic = new ListElementsAdapter(mainContext,R.layout.listview_item_row, list_tab_modern);
                	ListOfTabModern.addHeaderView(header);
                	ListOfTabModern.addFooterView(footer);
                	ListOfTabModern.setAdapter(ListAdapterClassic);
                	ListOfTabModern.setDivider(getResources().getDrawable(R.drawable.item_divide_horizontal));
                	ListOfTabModern.setDividerHeight(2);

                	int paddingPixel = 10;
                	float density = com.zlab.audiobooks.AudiobooksMain.mainContext.getResources().getDisplayMetrics().density;
                	int paddingDp = (int)(paddingPixel * density);
                	ListOfTabModern.setPadding(paddingDp,0,paddingDp,0);

                	ListOfTabModern.setOnItemClickListener(new OnItemClickListener() 
                    {
        				public void onItemClick(AdapterView<?> arg0, View arg1,
        						int position, long id) {
        							// Передача переменных активити деталей
        			                Intent detail = new Intent(com.zlab.audiobooks.AudiobooksMain.mainContext, com.zlab.audiobooks.AudiobooksDetail.class);
        			                detail.putExtra("BookName",		BookName[GenreIDModern[(int)id]]);
        			                detail.putExtra("Author", 		Author[GenreIDModern[(int)id]]);
        			                detail.putExtra("BookLogoURL",	BookLogoURL[GenreIDModern[(int)id]]);
        			                detail.putExtra("ReleaseDATA",	ReleaseDATA[GenreIDModern[(int)id]]);
        			                detail.putExtra("MediaURL",		MediaURL[GenreIDModern[(int)id]]);
        			                detail.putExtra("SizeKB",		SizeKB[GenreIDModern[(int)id]]);
        			                detail.putExtra("LanguageCODE", LanguageCODE[GenreIDModern[(int)id]]);
        			                detail.putExtra("BuildInPlayer", buildinplayer);
        			                startActivity(detail);
        				}
                    });
        		}
            } else
            if (Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("4")){
            	if (GenreLenFantastic!=0){
                	// Шапки
                	View header = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_header_row, null);
                	View footer = (View)getLayoutInflater(savedInstanceState).inflate(R.layout.listview_footer_row, null);

            		// Длинна списка
            		list_tab_fantastic = new ListElements[GenreLenFantastic];
                	// Заголовок с счетчиком
                	TextView txtheader = (TextView) header.findViewById(R.id.txtHeader);
                	txtheader.setText(getString(R.string.Header)+" "+GenreLenFantastic);
                	/*
                	txtheader.setTypeface(ptcaptionnormal);
                	*/
                	/*
                	BigDecimal SizeMBfloat = new BigDecimal(Integer.parseInt(SizeKB[i])/1024.00);
        			SizeMBfloat = SizeMBfloat.setScale(2, BigDecimal.ROUND_HALF_UP);
        			*/
                	for (int i=0; i<GenreLenFantastic; i++) {
                		list_tab_fantastic[i] = new ListElements(R.drawable.book_256, BookName[GenreIDFantastic[i]], Author[GenreIDFantastic[i]], "Размер:", SizeKB[GenreIDFantastic[i]]+" Мб");
                	}
                	ListOfTabFantastic = new ListView(getActivity());
                	ListElementsAdapter ListAdapterClassic = new ListElementsAdapter(mainContext,R.layout.listview_item_row, list_tab_fantastic);
                	ListOfTabFantastic.addHeaderView(header);
                	ListOfTabFantastic.addFooterView(footer);
                	ListOfTabFantastic.setAdapter(ListAdapterClassic);
                	ListOfTabFantastic.setDivider(getResources().getDrawable(R.drawable.item_divide_horizontal));
                	ListOfTabFantastic.setDividerHeight(2);

                	int paddingPixel = 10;
                	float density = com.zlab.audiobooks.AudiobooksMain.mainContext.getResources().getDisplayMetrics().density;
                	int paddingDp = (int)(paddingPixel * density);
                	ListOfTabFantastic.setPadding(paddingDp,0,paddingDp,0);

                	ListOfTabFantastic.setOnItemClickListener(new OnItemClickListener() 
                    {
        				public void onItemClick(AdapterView<?> arg0, View arg1,
        						int position, long id) {
        							// Передача переменных активити деталей
        			                Intent detail = new Intent(com.zlab.audiobooks.AudiobooksMain.mainContext, com.zlab.audiobooks.AudiobooksDetail.class);
        			                detail.putExtra("BookName",		BookName[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("Author", 		Author[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("BookLogoURL",	BookLogoURL[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("ReleaseDATA",	ReleaseDATA[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("MediaURL",		MediaURL[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("SizeKB",		SizeKB[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("LanguageCODE", LanguageCODE[GenreIDFantastic[(int)id]]);
        			                detail.putExtra("BuildInPlayer", buildinplayer);
        			                startActivity(detail);
        				}
                    });
        		}
            } else {
            }

        	// Строим весь список в ListView
/*
        	final ListElements list_o_tab_for_handle[] = list_tab_classic;

        	
*/
        	// Разделитель списка
        	/*
        	ListOfTab.setDivider(new ColorDrawable(Color.parseColor("#734819")));
        	ListOfTab.setDividerHeight(1);*/
        	
        	
        	// Фон списка
        	//ListOfTab.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_item));
        	
        	// Вывод содержимого таба
        	if (file.exists()!=true){
            	// Страница обновления
            	LayoutInflater inflateUpdatePage = (LayoutInflater) mainContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	updatePage = (RelativeLayout) inflateUpdatePage.inflate(R.layout.update_button, null);
        		return updatePage;
        			}else if(Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("1")){
        				return MainPage;
        				} else if(Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("2")){return ListOfTabClassic;
        				} else if(Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("3")){return ListOfTabModern;
        				} else if(Integer.toString(args.getInt(ARG_SECTION_NUMBER)).equals("4")){return ListOfTabFantastic;
        				} else {return updatePage;}

        }
    }

    public void getXML() {
    		  	    try {
    		  	    		// URI
	    		            if(dir.exists()==false) {dir.mkdirs();}
	    		            if(file.exists()==true) {file.delete();}
		    		  	    URL  url  = new URL("http://api.z-lab.me/app/adbook/zlab.audiobook.xml");

    		  	    		// STORAGE
	    		            URLConnection ucon = url.openConnection();
	    		            InputStream is = ucon.getInputStream();
	    		            BufferedInputStream bis = new BufferedInputStream(is);
	    		            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
	    		            int current = 0;
	    		            while ((current = bis.read()) != -1) {
	    		               baf.append((byte) current);
	    		            }

	    		            /* Convert the Bytes read to a String. */
	    		            FileOutputStream fos = new FileOutputStream(file);
	    		            fos.write(baf.toByteArray());
	    		            fos.flush();
	    		            fos.close();

    		  	            download_complete = true;
    		  	            m_ProgressLoadXML.dismiss();
    		  	            showParseXML.sendEmptyMessage(0);

    		  		     } catch (Exception ioe) {
    		  		    	download_complete = false;
    		  		    	m_ProgressLoadXML.dismiss();
    		  		    	handlMessage=getString(R.string.errNoInternet);
    	    		  		err.sendEmptyMessage(0);
    		  		    	 }
    	 }
    
    public void parseXML() {
    Document doc = null;
    		  	    try {
    		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();
    		  	            doc = db.parse(new InputSource(new InputStreamReader(new FileInputStream(file))));
    		  	            access_complete=true;
    		  		     } catch (Exception ioe) {
    		  		    	access_complete = false;
    		  		    	m_ProgressParseXML.dismiss();
    		  		    	handlMessage=getString(R.string.errNoSD);
    	    		  		err.sendEmptyMessage(0);
    		  		    	 }
    		  	    
    if (access_complete == true){
  	    try {
    		  		// Ноды библиотеки    		  			
    		  		NodeList LibNAMElist = doc.getElementsByTagName("LibNAME");
    		  		NodeList LibDiscriptionlist = doc.getElementsByTagName("LibDiscription");
    		  		NodeList LibLogoURLlist = doc.getElementsByTagName("LibLogoURL");
    		  		NodeList LibBookNumberlist = doc.getElementsByTagName("LibBookNumber");
    		  		NodeList LibBookStorageKBlist = doc.getElementsByTagName("LibBookStorageKB");
    		  		NodeList LibTypelist = doc.getElementsByTagName("LibType");
    		  		NodeList LibLastUpdatelist = doc.getElementsByTagName("LibLastUpdate");
    		  		NodeList LibXMLlist = doc.getElementsByTagName("LibXML");
    		  		NodeList LibWebURLlist = doc.getElementsByTagName("LibWebURL");

    		  		// Ноды книг
	    		  	NodeList GenreCODElist = doc.getElementsByTagName("GenreCODE");
	    		  	NodeList LanguageCODElist = doc.getElementsByTagName("LanguageCODE");
	    		  	NodeList SizeKBlist = doc.getElementsByTagName("SizeKB");
	    		  	NodeList BookNamelist = doc.getElementsByTagName("BookName");
	    		  	NodeList BookLogoURLlist = doc.getElementsByTagName("BookLogoURL");
	    		  	NodeList ReleaseDATAlist = doc.getElementsByTagName("ReleaseDATA");
	    		  	NodeList MediaURLlist = doc.getElementsByTagName("MediaURL");
	    		  	NodeList Authorlist = doc.getElementsByTagName("Author");

	    		  	// количество книг
    		  		len = BookNamelist.getLength();

    		        // Биндинг инфы о библиотеке
    		        Node LibNAMENode 			= LibNAMElist.item(0);			LibNAME			= LibNAMENode.getFirstChild().getNodeValue();
    		        Node LibDiscriptionNode 	= LibDiscriptionlist.item(0);	LibDiscription	= LibDiscriptionNode.getFirstChild().getNodeValue();
    		        Node LibLogoURLNode 		= LibLogoURLlist.item(0);		LibLogoURL		= LibLogoURLNode.getFirstChild().getNodeValue();
    		        Node LibBookNumberNode 		= LibBookNumberlist.item(0);	LibBookNumber	= LibBookNumberNode.getFirstChild().getNodeValue();
    		        Node LibBookStorageKBNode	= LibBookStorageKBlist.item(0);	LibBookStorageKB= LibBookStorageKBNode.getFirstChild().getNodeValue();
    		        Node LibTypeNode			= LibTypelist.item(0);			LibType			= LibTypeNode.getFirstChild().getNodeValue();
    		        Node LibLastUpdateNode		= LibLastUpdatelist.item(0);	LibLastUpdate	= LibLastUpdateNode.getFirstChild().getNodeValue();
    		        Node LibXMLNode				= LibXMLlist.item(0);			LibXML			= LibXMLNode.getFirstChild().getNodeValue();
    		        Node LibWebURLNode			= LibWebURLlist.item(0);		LibWebURL		= LibWebURLNode.getFirstChild().getNodeValue();

    		        /*--------------*/
    		        /* Списки книг  */
    		        /*--------------*/

    		  		// Жанры
    		        GenreCODE = new String[len];for (int i = 0; i < len; i++) {
    		        	Node GenreCODENode	=	GenreCODElist.item(i);
    		        		 GenreCODE[i]	=	GenreCODENode.getFirstChild().getNodeValue();}
    		        
    		        // Языки
    		        LanguageCODE = new String[len];for (int i = 0; i < len; i++) {
    		        	Node LanguageCODENode	=	LanguageCODElist.item(i);
    		        		 LanguageCODE[i]	=	LanguageCODENode.getFirstChild().getNodeValue();}
    		        
    		        // Размер
    		        SizeKB = new String[len];for (int i = 0; i < len; i++) {
    		        	Node SizeKBNode	=	SizeKBlist.item(i);
    		        		 SizeKB[i]	=	SizeKBNode.getFirstChild().getNodeValue();
    		        }
    		        
    		        // Названия
    		        BookName = new String[len];for (int i = 0; i < len; i++) {
    		        	Node BookNameNode	=	BookNamelist.item(i);
    		        		 BookName[i]	=	BookNameNode.getFirstChild().getNodeValue();
    		        }
    		        
    		        // Логотип
    		        BookLogoURL = new String[len];for (int i = 0; i < len; i++) {
    		        	Node BookLogoURLNode	=	BookLogoURLlist.item(i);
    		        		 BookLogoURL[i]		=	BookLogoURLNode.getFirstChild().getNodeValue();
    		        }
    		        
    		        // Даты издания
    		        ReleaseDATA = new String[len];for (int i = 0; i < len; i++) {
    		        	Node ReleaseDATANode	=	ReleaseDATAlist.item(i);
    		        		 ReleaseDATA[i]		=	ReleaseDATANode.getFirstChild().getNodeValue();
    		        }
    		  		
    		        // Ссылки
    		        MediaURL = new String[len];for (int i = 0; i < len; i++) {
    		        	Node MediaURLNode	=	MediaURLlist.item(i);
    		        		 MediaURL[i]	=	MediaURLNode.getFirstChild().getNodeValue();
    		        }
    		  		
    		        // Авторы
    		        Author = new String[len];for (int i = 0; i < len; i++) {
    		        	Node AuthorNode	=	Authorlist.item(i);
    		        		Author[i]	=	AuthorNode.getFirstChild().getNodeValue();
    		        }
    		        
    		        ////////////////////
                	// Забиваем счетчики
                	// Можно попробовать вместо отдельной переменной длинны списка, использовать .length
    		        GenreLenClassic=0;
    		        GenreLenModern=0;
    		        GenreLenFantastic=0;

                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("classic")){GenreLenClassic++;}}
                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("modern")){GenreLenModern++;}}
                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("fantastic")){GenreLenFantastic++;}}
                	// Забиваем жанры
                	GenreIDClassic = new Integer[GenreLenClassic];
                	GenreIDModern = new Integer[GenreLenModern];
                	GenreIDFantastic = new Integer[GenreLenFantastic];
                	int y=0;
                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("classic")){GenreIDClassic[y]=i;y++;}}y=0;
                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("modern")){GenreIDModern[y]=i;y++;}}y=0;
                	for (int i=0; i<len; i++) {if (GenreCODE[i].equals("fantastic")){GenreIDFantastic[y]=i;y++;}}y=0;

    		        ////////////////////

    		  		// Сообщение об окончании загрузки
    		        
    		        handlMessage=getString(R.string.UpdateComplete);
    		  		noerr.sendEmptyMessage(0);
    		        // Обновление
    		  		m_ProgressParseXML.dismiss();
    		        reloadHandler.sendEmptyMessage(0);
  	    	} catch (Exception ioe) {
  	    		m_ProgressParseXML.dismiss();
  		    	handlMessage=getString(R.string.errXMLparse);
		  		err.sendEmptyMessage(0);
	    	 }
    	}
    }
    
    
    public void getPrefs() {
        SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
        // Настройки темы
        SettingsThemeSelected = prefs.getString("SettingsThemeID", "0");
        buildinplayer = prefs.getBoolean("buildinplayer", true);
}
    
    public void setPrefs() {
        // Применяем настройки
    	
    	// Настройки оформления
    	/*
    	if		  (Integer.parseInt(SettingsThemeSelected) == 0) {
    	} else if (Integer.parseInt(SettingsThemeSelected) == 3) {
    	   setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        } else if (Integer.parseInt(SettingsThemeSelected) == 2) {
           setTheme(android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        } else if (Integer.parseInt(SettingsThemeSelected) == 1) {
           setTheme(android.R.style.Theme_Holo);
        }
    	*/

    }
    
    public void processXMLget() {
            new Thread() {
                public void run() {
                getXML();
                parseXML();
                }
            }.start();
     }
    
    protected void onResume() {
    	getPrefs();
    	super.onResume();
    }
    protected void Toasted() {
        Toast toast = Toast.makeText(getApplicationContext(), "STRING", Toast.LENGTH_SHORT);
        toast.show();
    }
    protected void onStop(){
        super.onStop();
        /*
        xml_store = getSharedPreferences("book", 0);
        xml_store_editor = xml_store.edit();
        xml_store_editor.putString("book", GenreCODE);
        xml_store_editor.commit();
        */
    }
    public void updateinbackground(){
    	showLoadXML.sendEmptyMessage(0);
    	updateLib = new Runnable(){
            public void run() {
            	AudiobooksMain.mainContext.processXMLget();
            }
        };
        Thread thread =  new Thread(null, updateLib, "MagentoBackground");
        thread.start();
    }
    public void parseinbackground(){
    	new Thread() {
            public void run() {
            parseXML();
            }
        }.start();
    }
    public void updateClickHandler(View view) {
        switch (view.getId()) {
        case R.id.UpdateButton:
        	updateinbackground();
        }
    }
    public void reload() {
    	Intent intent = getIntent();
  		finish();
  		startActivity(intent);
    }
    protected void setOrientation() {
        int current = getRequestedOrientation();
        // only switch the orientation if not in portrait
        if ( current != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        }
    }
    
}

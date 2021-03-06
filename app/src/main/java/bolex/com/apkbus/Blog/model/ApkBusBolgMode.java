package bolex.com.apkbus.Blog.model;

import android.content.Context;
import android.util.Log;

import com.vise.log.ViseLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bolex.com.apkbus.Blog.api.ApkBusBlog;
import bolex.com.apkbus.Blog.entity.ApkBusBlogItem;
import bolex.com.apkbus.Blog.entity.Banner;
import bolex.com.apkbus.MyApp;
import cn.wwah.basekit.base.model.BaseModel;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public class ApkBusBolgMode extends BaseModel {

    public ApkBusBolgMode(Context context) {
        super(context);
    }

    /**
     * 查询博客列表信息
     *
     * @param id
     * @param page
     * @param mObservable
     */
    public void findBlogList(final String id, final int page, Observer<List<ApkBusBlogItem>> mObservable) {
        bolex.com.apkbus.Blog.api.ApkBusBlog mApkBusBlog = MyApp.getApp().getRetrofit().create(bolex.com.apkbus.Blog.api.ApkBusBlog.class);
        Observable<List<ApkBusBlogItem>> responseBodyObservable = mApkBusBlog.getBlogList(id, page)
                .map(new Func1<ResponseBody, List<ApkBusBlogItem>>() {
                    @Override
                    public List<ApkBusBlogItem> call(ResponseBody responseBody) {
                        ArrayList<ApkBusBlogItem> mBlogs = new ArrayList<>();
                        try {
                            Document doc = Jsoup.parse(responseBody.string());
                            Elements total = doc.getElementsByClass("left");
                            Elements items = total.select("div");
                            for (Element element : items) {
                                if (element.className().equals("row")) {
                                    String authorHeadUrl = null, authorName = null, title, preview, time = null, readCount = null, commentCount = null, likeCount = null, url;
                                    Elements a = element.getElementsByTag("a");
                                    url = a.attr("href");
                                    title = a.select("h2").text();
                                    preview = element.getElementsByClass("preview").text();
                                    Elements info = element.getElementsByClass("info");
                                    authorHeadUrl = info.select("img").attr("src");
                                    Elements span = info.select("span");
                                    authorName = span.get(0).text();
                                    time = span.get(2).attr("title");
                                    readCount = span.get(3).text();
                                    commentCount = span.get(4).text();
                                    likeCount = span.get(5).text();
                                    readCount = readCount.substring(readCount.length() - 1);
                                    commentCount = commentCount.substring(commentCount.length() - 1);
                                    likeCount = likeCount.substring(likeCount.length() - 1);
                                    ApkBusBlogItem item = new ApkBusBlogItem(authorHeadUrl, authorName, title, preview, time, readCount, commentCount, likeCount, url);
                                    mBlogs.add(item);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return mBlogs;
                    }
                });

        subscribe(responseBodyObservable, mObservable);
    }

    /**
     * 取banner
     * @param mObservable
     */
    public void findHomeBanner(Observer<List<Banner>> mObservable) {
        ApkBusBlog apkBusBlog = MyApp.getApp().getRetrofit().create(ApkBusBlog.class);
        Observable<List<Banner>> responseBodyObservable = apkBusBlog.getBanner().map(new Func1<ResponseBody, List<Banner>>() {

            @Override
            public List<Banner> call(ResponseBody responseBody) {
                ArrayList<Banner> mBanner = new ArrayList<>();
                try {
                    Document parse = Jsoup.parse(responseBody.string());
                    Element theTarget = parse.getElementById("theTarget");
                    Elements items = theTarget.children();
                    String url, title, imgUrl;
                    for (Element item : items) {
                        Elements a = item.getElementsByTag("a");
                        url = a.attr("href");
                        imgUrl = a.select("img").attr("src");
                        if (!imgUrl.contains("http")) {
                            imgUrl = MyApp.getApp().getBaseUrl() + imgUrl;
                        }
                        title = a.select("span").text();
                        if (!url.isEmpty() || !imgUrl.isEmpty()) {
                            mBanner.add(new Banner(url, title, imgUrl));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return mBanner;
            }

        });


        subscribe(responseBodyObservable, mObservable);
    }


}
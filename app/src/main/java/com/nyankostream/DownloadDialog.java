package com.nyankostream;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DownloadDialog {

    public static void showDownloadDialog(Context context, String responseJson) {
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(responseJson);
            JSONObject data = jsonResponse.getJSONObject("data");
            String batchTitle = data.getString("batch");
            JSONArray downloadUrls = data.getJSONArray("download_urls");

            // Create a list of download options
            List<DownloadOption> downloadOptionList = new ArrayList<>();
            for (int i = 0; i < downloadUrls.length(); i++) {
                JSONObject downloadOptionJson = downloadUrls.getJSONObject(i);
                String resolution = downloadOptionJson.getString("resolution");
                String fileSize = downloadOptionJson.getString("file_size");
                JSONArray urls = downloadOptionJson.getJSONArray("urls");
                for (int j = 0; j < urls.length(); j++) {
                    JSONObject urlObj = urls.getJSONObject(j);
                    String provider = urlObj.getString("provider");
                    String url = urlObj.getString("url");
                    downloadOptionList.add(new DownloadOption(resolution, fileSize, provider, url));
                }
            }

            // Create the dialog view
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_download_options, null);

            // Set batch title
            TextView tvBatchTitle = dialogView.findViewById(R.id.tv_batch_title);
            tvBatchTitle.setText(batchTitle);

            // Populate the ListView with download options
            ListView lvDownloadOptions = dialogView.findViewById(R.id.lv_download_options);
            DownloadOptionAdapter adapter = new DownloadOptionAdapter(context, downloadOptionList);
            lvDownloadOptions.setAdapter(adapter);

            // Show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);
            builder.setPositiveButton("Close", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


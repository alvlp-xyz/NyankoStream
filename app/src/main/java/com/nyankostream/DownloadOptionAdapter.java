package com.nyankostream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class DownloadOptionAdapter extends BaseAdapter {

    private Context context;
    private List<DownloadOption> downloadOptionList;

    public DownloadOptionAdapter(Context context, List<DownloadOption> downloadOptionList) {
        this.context = context;
        this.downloadOptionList = downloadOptionList;
    }

    @Override
    public int getCount() {
        return downloadOptionList.size();
    }

    @Override
    public Object getItem(int position) {
        return downloadOptionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_download_option, parent, false);
        }

        // Get the current download option
        DownloadOption option = downloadOptionList.get(position);

        // Set resolution, file size, and provider
        TextView tvResolution = convertView.findViewById(R.id.tv_resolution);
        TextView tvFileSize = convertView.findViewById(R.id.tv_file_size);
        TextView tvProvider = convertView.findViewById(R.id.tv_provider);
        Button btnDownload = convertView.findViewById(R.id.btn_download);

        tvResolution.setText(option.getResolution());
        tvFileSize.setText(option.getFileSize());
        tvProvider.setText(option.getProvider());

        // Set download button click listener
        btnDownload.setOnClickListener(v -> {
            // Open the download URL in the browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(option.getUrl()));
            context.startActivity(browserIntent);
        });

        return convertView;
    }
}

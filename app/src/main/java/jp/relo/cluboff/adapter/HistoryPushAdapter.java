package jp.relo.cluboff.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import framework.phvtUtils.AppLog;
import framework.phvtUtils.StringUtil;
import jp.relo.cluboff.R;
import jp.relo.cluboff.database.MyDatabaseHelper;
import jp.relo.cluboff.model.HistoryPushDTO;
import jp.relo.cluboff.util.Constant;
import jp.relo.cluboff.util.Utils;

/**
 * Created by tonkhanh on 6/8/17.
 */

public class HistoryPushAdapter extends RecyclerView.Adapter<HistoryPushAdapter.HisViewHolder>{
    ArrayList<HistoryPushDTO> listData;
    Context mContext;
    private MyDatabaseHelper myDatabaseHelper;
    iCallDetailCoupon miCallDetailCoupon;
    iCallDismiss miCallDismiss;

    public HistoryPushAdapter(Context mContext,ArrayList<HistoryPushDTO> listData, iCallDetailCoupon miCallDetailCoupon, iCallDismiss miCallDismiss) {
        this.listData = listData;
        this.mContext = mContext;
        myDatabaseHelper = new MyDatabaseHelper(mContext);
        this.miCallDetailCoupon = miCallDetailCoupon;
        this.miCallDismiss = miCallDismiss;
    }

    @Override
    public HisViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_push_row, parent, false);

        return new HisViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HisViewHolder holder, int position) {
        final HistoryPushDTO data = listData.get(position);
        holder.tvHistoryTime.setText(Utils.long2Time(data.getTimeHis()));
        holder.tvHistoryContent.setText(data.getContentHis());
        if(StringUtil.isEmpty(data.getwHis())){
            holder.tvHistoryLink.setVisibility(View.GONE);
        }else{
            holder.tvHistoryLink.setVisibility(View.VISIBLE);
        }
        holder.tvHistoryLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String target = data.getwHis();
                if(miCallDetailCoupon !=null&&!Constant.TARGET_PUSH_SCREEN_LIST.equalsIgnoreCase(target)){
                    if(Constant.TARGET_PUSH_SCREEN_AREA.equalsIgnoreCase(target)){
                        miCallDetailCoupon.callbackDetail(0);
                    }else if(Constant.TARGET_PUSH_SCREEN_SITE.equalsIgnoreCase(target)){
                        miCallDetailCoupon.callbackDetail(2);
                    }else{
                        miCallDetailCoupon.callbackDetail(1);
                    }
                    if(miCallDismiss!=null){
                        miCallDismiss.callDismiss();
                    }
                }
            }
        });
    }
    public void setNotifyDataSetChanged(ArrayList<HistoryPushDTO> listData){
        this.listData = listData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class HisViewHolder extends RecyclerView.ViewHolder {
        public TextView tvHistoryTime, tvHistoryContent, tvHistoryLink;

        public HisViewHolder(View view) {
            super(view);
            tvHistoryTime = (TextView) view.findViewById(R.id.tvHistoryTime);
            tvHistoryContent = (TextView) view.findViewById(R.id.tvHistoryContent);
            tvHistoryLink = (TextView) view.findViewById(R.id.tvHistoryLink);
            tvHistoryLink.setPaintFlags(tvHistoryLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
    public interface iCallDetailCoupon{
        void callbackDetail(int tabIndex);
    }
    public interface iCallDismiss{
        void callDismiss();
    }

}

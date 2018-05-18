package it.unitn.simob.howsthere.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {

    private Context mContext;
    private List<Feed> feedList;
    private Context con;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, location, timeStamp;
        public ImageView image;
        public ProgressBar po;
        public String ID;
        public ImageView heart;

        public MyViewHolder(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.cardView_name);
            location = (TextView) view.findViewById(R.id.cardView_location);
            timeStamp = (TextView) view.findViewById(R.id.cardView_timestamp);
            image = (ImageView) view.findViewById(R.id.cardView_image);
            po = (ProgressBar) view.findViewById(R.id.progressBar2);
            heart = (ImageView) view.findViewById(R.id.heart);
            con = view.getContext();

            image.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(con, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        ImageViewAnimatedChange(view.getContext(), heart, R.drawable.icon_heart_fill);
                        return super.onDoubleTap(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewAnimatedChange(view.getContext(), heart, R.drawable.icon_heart_fill);
                    System.out.println("Cazzo: " + ID);
                }
            });
        }

    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final int new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageResource(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public FeedAdapter(Context mContext, List<Feed> feedList) {
        this.mContext = mContext;
        this.feedList = feedList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate new view when we create new items in our recyclerview
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_feed, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Feed feed = feedList.get(position);
        holder.ID = feed.getID();
        holder.name.setText(feed.getName());
        holder.location.setText(feed.getLocation());

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        long diff = 0;
        try {
            Date date = format.parse(feed.getTimeStamp());
            long diffInMillies = Math.abs(new Date().getTime() - date.getTime());
            diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            System.out.println("Giorni diff: " + diff);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.timeStamp.setText("" + diff + " ore");

        //Libreria per la gestione ottimizzata delle immagini
        Glide.with(mContext)
                .load(feed.getImageUrl())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.po.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.po.setVisibility(View.GONE);
                        return false;
                    }
                })
                .fitCenter()
                .placeholder(R.drawable.placeholder)
                .priority(Priority.LOW)
                .into(holder.image);

        //Picasso.get().load(feed.getImageUrl()).placeholder(R.drawable).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

}
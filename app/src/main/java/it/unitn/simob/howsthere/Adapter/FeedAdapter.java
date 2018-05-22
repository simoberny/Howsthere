package it.unitn.simob.howsthere.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.transition.Visibility;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.sql.SQLOutput;
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
    DatabaseReference feed;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, location, timeStamp;
        public ImageView image, menu;
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
            menu = view.findViewById(R.id.cardView_dots);
            con = view.getContext();

            image.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(con, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        ImageViewAnimatedChange(view.getContext(), heart, R.drawable.icon_heart_fill);
                        ImageViewAnimatedChange(view.getContext(), image, image.getDrawable());
                        makeLike(ID);
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
                    makeLike(ID);
                }
            });
        }

    }

    public void makeLike(final String id){
        if(currentUser != null){
            db.collection("feeds").document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Feed temp = documentSnapshot.toObject(Feed.class);
                            temp.setID(id);

                            List<String> liked = temp.getLikes_id();

                            if(!liked.contains(currentUser.getUid())){
                                temp.setLikes(temp.getLikes() + 1);
                                temp.add_user_to_like(currentUser.getUid());

                                db.collection("feeds").document(id)
                                        .set(temp)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                System.out.println("Feed aggiornata!");
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Drawable new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageDrawable(new_image);
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

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate new view when we create new items in our recyclerview
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_feed, parent, false);
        db = FirebaseFirestore.getInstance();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Feed feed = feedList.get(position);
        holder.ID = feed.getID();
        holder.name.setText(feed.getName());
        holder.location.setText(feed.getLocation());
        db.collection("feeds").document(holder.ID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Feed f = documentSnapshot.toObject(Feed.class);
                        if(f != null && f.getLikes_id().contains(currentUser.getUid())){
                            holder.heart.setImageResource(R.drawable.icon_heart_fill);
                        }
                    }
                });

        if(!currentUser.getUid().equals(feed.getUid())){
            holder.menu.setVisibility(View.GONE);
        }

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.menu,position, feed.getID());
            }
        });



        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        long diff = 0;
        try {
            Date date = format.parse(feed.getTimeStamp());
            System.out.println(date);
            long diffInMillies = Math.abs(new Date().getTime() - date.getTime());
            diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String tempo = "";

        if(diff < 60){
            tempo = diff + " min";
        }else if(diff >= 60 && (diff/60) < 24){
            tempo = (diff / 60) + " ore";
        }else{
            tempo = (diff / 60 / 24) + " giorni";
        }

        holder.timeStamp.setText(tempo);

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
                .placeholder(R.drawable.placeholder)
                .priority(Priority.LOW)
                .into(holder.image);
    }

    private void showPopupMenu(View view,int position, String id) {
        PopupMenu popup = new PopupMenu(view.getContext(),view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_feed, popup.getMenu());
        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new FeedItemClickListener(position, id));
        popup.show();
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    class FeedItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private int position;
        private String id;
        public FeedItemClickListener(int positon, String id) {
            this.position=positon;
            this.id = id;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.view_image:
                    return true;
                case R.id.delete_feed:
                    db.collection("feeds").document(id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    feedList.remove(position);
                                    notifyItemRemoved(position);
                                    Log.d("DELETE", "DocumentSnapshot successfully deleted!");
                                }
                            });
                    return true;
                default:
            }
            return false;
        }
    }

}


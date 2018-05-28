package it.unitn.simob.howsthere.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.R;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {
    private Context mContext;
    private List<Feed> feedList;
    private Context con;
    DatabaseReference feed;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, location, timeStamp, descrizione, likes;
        public ImageView menu;
        public ImageView image;
        public ProgressBar po;
        public String ID;
        public ImageView heart;

        public MyViewHolder(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.cardView_name);
            descrizione = view.findViewById(R.id.descrizione);
            location = (TextView) view.findViewById(R.id.cardView_location);
            timeStamp = (TextView) view.findViewById(R.id.cardView_timestamp);
            image = (ImageView) view.findViewById(R.id.cardView_image);
            po = (ProgressBar) view.findViewById(R.id.progressBar2);
            heart = (ImageView) view.findViewById(R.id.heart);
            menu = view.findViewById(R.id.cardView_dots);
            likes = view.findViewById(R.id.likes);
            con = view.getContext();
        }
    }

    public FeedAdapter(Context mContext, List<Feed> feedList) {
        this.mContext = mContext;
        this.feedList = feedList;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_feed, parent, false);
        db = FirebaseFirestore.getInstance();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Feed feed = feedList.get(position);
        holder.ID = feed.getID();
        holder.name.setText(feed.getName());
        holder.location.setText(feed.getLocation());
        holder.descrizione.setText(feed.getDescrizione());
        holder.likes.setText(feed.getLikes() + " Mi piace");

        holder.image.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(con, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    openGallery(position);
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    ImageViewAnimatedChange(con, holder.image, holder.image.getDrawable(), 0);
                    makeLike(holder.ID, position, holder.heart);
                    return super.onDoubleTap(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeLike(holder.ID, position, holder.heart);
            }
        });

        if(currentUser != null) {
            db.collection("feeds").document(holder.ID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Feed f = documentSnapshot.toObject(Feed.class);
                            if (f != null && f.getLikes_id().contains(currentUser.getUid())) {
                                holder.heart.setImageResource(R.drawable.icon_heart_fill);
                                System.out.println("LIKE feed " + holder.ID + " : SI");
                            } else {
                                holder.heart.setImageResource(R.drawable.icon_heart);
                                System.out.println("LIKE feed " + holder.ID + " : NO");
                            }
                        }
                    });
            if(!currentUser.getUid().equals(feed.getUid())){
                holder.menu.setVisibility(View.GONE);
            }
        }else{
            holder.heart.setVisibility(View.GONE);
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
            long temp = (diff / 60 / 24);
            tempo = temp + " giorn" + (((int)temp == 1) ? "o" : "i");
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
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

    public void makeLike(final String id, final int position, final ImageView img){
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
                                ImageViewAnimatedChange(con, img, null, R.drawable.icon_heart_fill);
                            }else{
                                temp.setLikes(temp.getLikes() - 1);
                                temp.remove_user_to_like(currentUser.getUid());
                                ImageViewAnimatedChange(con, img, null, R.drawable.icon_heart);
                            }

                            feedList.set(position, temp);
                            notifyItemChanged(position);

                            db.collection("feeds").document(id)
                                    .set(temp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            System.out.println("Feed aggiornata!");
                                        }
                                    });
                        }
                    });
        }else{
            Snackbar mySnackbar = Snackbar.make(((MainActivity)mContext).findViewById(R.id.frame_layout), "Esegui il login per lasciare like alle foto!", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    public static void ImageViewAnimatedChange(Context c, final ImageView v, final Drawable new_image, final int new_image_id) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                if(new_image_id == 0){
                    v.setImageDrawable(new_image);
                }else{
                    v.setImageResource(new_image_id);
                }

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
                    openGallery(position);
                    return true;
                case R.id.download:
                    Glide.with(mContext)
                            .load(feedList.get(position).getImageUrl())
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation)  {
                                    saveImage(resource);
                                }
                            });
                    break;
                case R.id.delete_feed:
                    db.collection("feeds").document(id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference();

                                    String file_name = UUID.randomUUID() + ".jpg";
                                    final StorageReference delRef = storageRef.child("images/" +feedList.get(position).getFile_name());
                                    delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            feedList.remove(position);
                                            notifyItemRemoved(position);
                                            Snackbar mySnackbar = Snackbar.make(((MainActivity)mContext).findViewById(R.id.frame_layout), "Feed eliminata!", Snackbar.LENGTH_SHORT);
                                            mySnackbar.show();
                                        }
                                    });
                                }
                            });
                    return true;
                default:
            }
            return false;
        }
    }

    private String saveImage(Bitmap image) {
        String savedImagePath = null;

        String imageFileName = "JPEG_" + "FILE_NAME" + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/Howsthere");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            galleryAddPic(savedImagePath);
            Snackbar mySnackbar = Snackbar.make(((MainActivity)mContext).findViewById(R.id.frame_layout), "Foto salvata nella galleria!", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
        return savedImagePath;
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        con.sendBroadcast(mediaScanIntent);
    }

    public void openGallery(int posizione){
        new ImageViewer.Builder(mContext, feedList)
            .setFormatter(new ImageViewer.Formatter<Feed>() {
                @Override
                public String format(Feed customImage) {
                    return customImage.getImageUrl();
                }
            })
            .setStartPosition(posizione-1).show();
    }
}


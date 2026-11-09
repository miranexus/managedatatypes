package com.example.managedatatypes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    private final List<Object> listRecyclerItem;

    @SuppressLint("StaticFieldLeak")
    public static Context cText;
    @SuppressLint("StaticFieldLeak")
    public static TextView nameViewUpdate;
    @SuppressLint("StaticFieldLeak")
    public static TextView passwordViewUpdate;

    public RecyclerAdapter(Context cText, List<Object> listRecyclerItem) {
        RecyclerAdapter.cText = cText;
        this.listRecyclerItem = listRecyclerItem;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        private final TextView date;
        private final TextView name;
        private final TextView email;
        private final TextView password;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            password = itemView.findViewById(R.id.password);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        @SuppressLint("InflateParams")
        @Override
        public void onClick(View v) {

            nameViewUpdate = v.findViewById(R.id.name);
            passwordViewUpdate = v.findViewById(R.id.password);

            TextView emailView = v.findViewById(R.id.email);
            String emailText = emailView.getText().toString();
            
            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.SQLiteActivityRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();
                SQLiteActivity.handleSQLiteUpdateDialog(v,cText,emailText);
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MongoActivityRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();
                MongoActivity.handleMongoUpdateDialog(v,cText,emailText);
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MySQLPHPActivityRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();
                MySQLPHPActivity.handleMySQLPHPUpdateDialog(v,cText,emailText);
            }

            //******************************************************************************************************

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.SQLiteCalendarRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();

                CalendarFragment.handleSQLiteCalendarUpdateDialog(v,cText,emailText);
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MongoCalendarRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();

                CalendarFragment.handleMongoCalendarUpdateDialog(v,cText,emailText);
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MySQLPHPCalendarRecycle))){
                LayoutInflater inflater = (LayoutInflater)cText.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(inflater != null){
                    v =  inflater.inflate(R.layout.dialog_update_activity, null);
                }
                new AlertDialog.Builder(cText)
                        .setCancelable(true)
                        .setView(v).show();

                CalendarFragment.handleMySQLPHPCalendarUpdateDialog(v,cText,emailText);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            TextView nameView = v.findViewById(R.id.name);
            TextView emailView = v.findViewById(R.id.email);
            TextView passwordView = v.findViewById(R.id.password);

            String nameText = nameView.getText().toString();
            String emailText = emailView.getText().toString();
            String passwordText = passwordView.getText().toString();

            SQLiteDatabaseHelper myDBAll = new SQLiteDatabaseHelper(cText);

            // ACTIVATES IF "public static String recycleDelete" HAS BEEN ACCESSED FROM "SQLiteActivity"
            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.SQLiteActivityRecycle)) ||
                    MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.SQLiteCalendarRecycle))){

                new AlertDialog.Builder(cText)
                        .setMessage("Delete this entry?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            int position = getBindingAdapterPosition();
                            if (position == RecyclerView.NO_POSITION) {
                                Log.w(TAG, "Cannot delete an entry that is no longer displayed");
                                return;
                            }
                            // GET THE ID OF THE EMAIL OF THE ITEM CLICKED ON THE RECYCLERVIEW
                            String userId = myDBAll.getUserFieldByEmail(emailText, SQLiteContract.SaveUsersTable._ID);
                            if (userId == null) {
                                Toast.makeText(cText, cText.getResources().getString(R.string.noentries),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            // DELETE THE USER BASED ON THE ID PROVIDED
                            myDBAll.deleteUser(Integer.parseInt(userId));
                            Toast.makeText(cText, cText.getResources().getString(R.string.entrydelete),
                                    Toast.LENGTH_LONG).show();
                            // REMOVE THE ITEM FROM THE RECYCLER VIEW
                            listRecyclerItem.remove(position);
                            notifyItemRemoved(position);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MongoActivityRecycle)) ||
                    MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MongoCalendarRecycle))){

                new AlertDialog.Builder(cText)
                        .setMessage("Delete this entry?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            int position = getBindingAdapterPosition();
                            if (position == RecyclerView.NO_POSITION) {
                                Log.w(TAG, "Cannot delete an entry that is no longer displayed");
                                return;
                            }
                            MongoActivity.handleMongoDeleteEmailDialog(nameText, emailText, passwordText);
                            //Toast.makeText(cText, "Mongo Activity = " + emailText + " deleted",
                                    //Toast.LENGTH_LONG).show();
                            // REMOVE THE ITEM FROM THE RECYCLER VIEW
                            listRecyclerItem.remove(position);
                            notifyItemRemoved(position);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            if (MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MySQLPHPActivityRecycle)) ||
                    MainActivity.recycleDelete.equals(cText.getResources().getString(R.string.MySQLPHPCalendarRecycle))){

                new AlertDialog.Builder(cText)
                        .setMessage("Delete this entry?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            int position = getBindingAdapterPosition();
                            if (position == RecyclerView.NO_POSITION) {
                                Log.w(TAG, "Cannot delete an entry that is no longer displayed");
                                return;
                            }
                            MySQLPHPActivity.handleMySQLPHPDeleteEmailDialog(nameText, emailText, passwordText);
                            //Toast.makeText(cText, "MySQLPHP Activity = " + emailText + " deleted",
                                    //Toast.LENGTH_LONG).show();
                            // REMOVE THE ITEM FROM THE RECYCLER VIEW
                            listRecyclerItem.remove(position);
                            notifyItemRemoved(position);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            return false;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.recycler_list_item, viewGroup, false);
        return new ItemViewHolder((layoutView));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        RecyclerVariables variables = (RecyclerVariables) listRecyclerItem.get(i);

        itemViewHolder.date.setText(variables.getDate());
        itemViewHolder.name.setText(variables.getName());
        itemViewHolder.email.setText(variables.getEmail());
        itemViewHolder.password.setText(variables.getPassword());
    }

    @Override
    public int getItemCount() {
        return listRecyclerItem.size();
    }
}

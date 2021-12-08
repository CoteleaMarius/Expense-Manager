package com.example.expensemanager;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class ExpenseFragment extends Fragment {
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;
    //Recycleview
    private RecyclerView recyclerView;

    private TextView expenseSumResult;

    //Edit data item
    private EditText edtAmount;
    private Spinner spType;
    private EditText edtNote;
    private Button btnUpdate;
    private Button btnDelete;
    //Data variables
    private String type;
    private String note;
    private int amount;
    private String postKey;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    public static ExpenseFragment newInstance(String param1, String param2) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);
        expenseSumResult = myView.findViewById(R.id.expense_txt_result);
        recyclerView = myView.findViewById(R.id.recycler_id_expense);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int expenseSum = 0;

                for (DataSnapshot mySnapshot : snapshot.getChildren()) {
                    Data data = mySnapshot.getValue(Data.class);
                    expenseSum += data.getAmount();
                    String strExpenseSum = String.valueOf(expenseSum);
                    expenseSumResult.setText(strExpenseSum);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class,
                        R.layout.expense_recycler_data,
                        MyViewHolder.class,
                        mExpenseDatabase
                ) {
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, Data model, int position) {
                myViewHolder.setDate(model.getDate());
                myViewHolder.setType(model.getType());
                myViewHolder.setNote(model.getNote());
                myViewHolder.setAmount(model.getAmount());
                myViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postKey = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();
                        updateDataItem();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }

        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }

        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.Note_txt_expense);
            mNote.setText(note);
        }

        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.ammount_txt_expense);
            String strAmount = String.valueOf(amount);
            mAmount.setText(strAmount);
        }

    }

    private void updateDataItem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.update_data_item, null);
        myDialog.setView(myView);
        edtAmount = myView.findViewById(R.id.amount_edt);
        edtNote = myView.findViewById(R.id.note_edt);
        spType = myView.findViewById(R.id.updateDropdown);
        spType.setSelection(0);
        edtNote.setText(note);
        edtNote.setSelection(note.length());
        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());
        btnUpdate = myView.findViewById(R.id.btnUpdate);
        btnDelete = myView.findViewById(R.id.btnDelete);
        final AlertDialog dialog = myDialog.create();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = spType.getSelectedItem().toString().trim();
                note = edtNote.getText().toString().trim();
                String stAmount = String.valueOf(amount);
                stAmount = edtAmount.getText().toString().trim();
                int intAmount = Integer.parseInt(stAmount);
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(intAmount, type, note, postKey, mDate);
                mExpenseDatabase.child(postKey).setValue(data);
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpenseDatabase.child(postKey).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
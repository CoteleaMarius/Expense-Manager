package com.example.expensemanager;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class IncomeFragment extends Fragment {
    private CheckBox checkBox;
    private EditText editText;
    private boolean isOpen = false;
    private TextView fab_income_txt;
    private Animation FadOpen, FadClose;
    private FloatingActionButton fab_main;
    //Firebase database
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    //Recyclerview
    private RecyclerView recyclerView;
    //TextView
    private TextView incomeSum;
    //Update edit text
    private EditText edtAmount;
    private Spinner spType;
    private EditText edtNote;
    //Update and Delete button
    private Button btnUpdate;
    private Button btnDelete;
    //Data item value
    private String type;
    private String note;
    private int amount;
    private String postKey;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //Floating action buttons
    private FloatingActionButton fab_income_btn;

    private String mParam1;
    private String mParam2;

    public IncomeFragment() {
        // Required empty public constructor
    }

    public static IncomeFragment newInstance(String param1, String param2) {
        IncomeFragment fragment = new IncomeFragment();
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
        View myView = inflater.inflate(R.layout.fragment_income, container, false);
        fab_income_txt = myView.findViewById(R.id.income_ft_text);
        fab_income_btn = myView.findViewById(R.id.income_ft_btn);
        FadOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        FadClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);
        fab_main = myView.findViewById(R.id.fb_main_plus_btn);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
                if (isOpen) {
                    fab_income_btn.startAnimation(FadClose);
                    fab_income_btn.setClickable(false);
                    fab_income_txt.startAnimation(FadClose);
                    fab_income_txt.setClickable(false);
                    isOpen = false;
                } else {
                    fab_income_btn.startAnimation(FadOpen);
                    fab_income_btn.setClickable(true);
                    fab_income_txt.startAnimation(FadOpen);
                    fab_income_txt.setClickable(true);
                    isOpen = true;
                }
            }
        });
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        incomeSum = myView.findViewById(R.id.income_txt_result);
        recyclerView = myView.findViewById(R.id.recycler_id_income);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalValue = 0;
                for (DataSnapshot mySnapshot : snapshot.getChildren()) {

                    Data data = mySnapshot.getValue(Data.class);
                    totalValue += data.getAmount();
                    String stTotalValue = String.valueOf(totalValue);
                    incomeSum.setText(stTotalValue);
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
                        R.layout.income_recycler_data,
                        MyViewHolder.class,
                        mIncomeDatabase
                ) {
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, Data model, int position) {
                myViewHolder.setType(model.getType());
                myViewHolder.setNote(model.getNote());
                myViewHolder.setDate(model.getDate());
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

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_income);
            mType.setText(type);
        }

        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.Note_txt_income);
            mNote.setText(note);
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_income);
            mDate.setText(date);
        }

        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.ammount_txt_income);
            String stAmount = String.valueOf(amount);
            mAmount.setText(stAmount);
        }
    }

    private void updateDataItem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.update_data_item, null);
        myDialog.setView(myView);
        edtAmount = myView.findViewById(R.id.amount_edt);
        spType = myView.findViewById(R.id.updateDropdown);
        spType.setVisibility(View.VISIBLE);
        checkBox = myView.findViewById(R.id.updateCheckbox);
        editText = myView.findViewById(R.id.editTextUpdate);
        myView.findViewById(R.id.updateDropdown2).setVisibility(View.INVISIBLE);
        edtNote = myView.findViewById(R.id.note_edt);
        //Set data edit text
        spType.setSelection(0);
        edtNote.setText(note);
        edtNote.setSelection(note.length());
        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myView.findViewById(R.id.btnUpdate);
        btnDelete = myView.findViewById(R.id.btnDelete);
        final AlertDialog dialog = myDialog.create();
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkBox.isChecked()){
                    spType.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                }else{
                    spType.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note = edtNote.getText().toString().trim();
                String mdAmount = String.valueOf(amount);
                mdAmount = edtAmount.getText().toString().trim();
                if(checkBox.isChecked()){
                    type = editText.getText().toString().trim();
                }else{
                    type = spType.getSelectedItem().toString().trim();
                }
                int myAmount = Integer.parseInt(mdAmount);
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(myAmount, type, note, postKey, mDate);
                mIncomeDatabase.child(postKey).setValue(data);
                dialog.dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIncomeDatabase.child(postKey).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void ftAnimation() {
        if (isOpen) {
            fab_income_btn.startAnimation(FadClose);
            fab_income_btn.setClickable(false);
            fab_income_txt.startAnimation(FadClose);
            fab_income_txt.setClickable(false);
            isOpen = false;
        } else {
            fab_income_btn.startAnimation(FadOpen);
            fab_income_btn.setClickable(true);
            fab_income_txt.startAnimation(FadOpen);
            fab_income_txt.setClickable(true);
            isOpen = true;
        }
    }

    public void incomeDataInsert() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        checkBox = myView.findViewById(R.id.category_check);
        editText = myView.findViewById(R.id.edittextCategory);
        final EditText edtAmount = myView.findViewById(R.id.amount_edt);
        final Spinner edtType = myView.findViewById(R.id.dropdown);
        edtType.setVisibility(View.VISIBLE);
        myView.findViewById(R.id.dropdown2).setVisibility(View.INVISIBLE);
        final EditText edtNote = myView.findViewById(R.id.note_edt);
        Button btnSave = myView.findViewById(R.id.btnSave);
        Button btnCancel = myView.findViewById(R.id.btnCancel);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.isChecked()) {
                    edtType.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                } else {
                    edtType.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type;
                String amount = edtAmount.getText().toString().trim();
                String note = edtNote.getText().toString().trim();
                if (checkBox.isChecked()) {
                    type = editText.getText().toString().trim();
                } else {
                    type = edtType.getSelectedItem().toString().trim();
                }
                if (TextUtils.isEmpty(type)) {
                    Toast.makeText(getActivity(), "Select a type", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    edtAmount.setError("Required field");
                    return;
                }
                int ourammountint = Integer.parseInt(amount);

                if (TextUtils.isEmpty(note)) {
                    edtNote.setError("Required field");
                    return;
                }
                String id = mIncomeDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(ourammountint, type, note, id, mDate);
                mIncomeDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftAnimation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void addData() {
        //Fab Button income
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomeDataInsert();
            }
        });
    }
}
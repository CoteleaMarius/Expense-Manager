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

public class ExpenseFragment extends Fragment {
    private CheckBox checkBox;
    private EditText editText;
    private boolean isOpen = false;
    private TextView fab_expense_txt;
    private Animation FadOpen, FadClose;
    private FloatingActionButton fab_main;
    private FloatingActionButton fab_expense_btn;
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
        fab_expense_txt = myView.findViewById(R.id.expense_ft_text);
        fab_main = myView.findViewById(R.id.fb_main_plus_btn);
        fab_expense_btn = myView.findViewById(R.id.expense_ft_btn);
        FadOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        FadClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
                if (isOpen) {
                    fab_expense_btn.startAnimation(FadClose);
                    fab_expense_btn.setClickable(false);
                    fab_expense_txt.startAnimation(FadClose);
                    fab_expense_txt.setClickable(false);
                    isOpen = false;
                } else {
                    fab_expense_btn.startAnimation(FadOpen);
                    fab_expense_btn.setClickable(true);
                    fab_expense_txt.startAnimation(FadOpen);
                    fab_expense_txt.setClickable(true);
                    isOpen = true;
                }
            }
        });
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
        spType = myView.findViewById(R.id.updateDropdown2);
        spType.setVisibility(View.VISIBLE);
        myView.findViewById(R.id.updateDropdown).setVisibility(View.INVISIBLE);
        checkBox = myView.findViewById(R.id.updateCheckbox);
        editText = myView.findViewById(R.id.editTextUpdate);
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
                if (checkBox.isChecked()) {
                    spType.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                } else {
                    spType.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = spType.getSelectedItem().toString().trim();
                note = edtNote.getText().toString().trim();
                String stAmount = String.valueOf(amount);
                stAmount = edtAmount.getText().toString().trim();
                if(checkBox.isChecked()){
                    type = editText.getText().toString().trim();
                }else{
                    type = spType.getSelectedItem().toString().trim();
                }
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

    private void ftAnimation() {
        if (isOpen) {
            fab_expense_btn.startAnimation(FadClose);
            fab_expense_btn.setClickable(false);
            fab_expense_txt.startAnimation(FadClose);
            fab_expense_txt.setClickable(false);
            isOpen = false;
        } else {
            fab_expense_btn.startAnimation(FadOpen);
            fab_expense_btn.setClickable(true);
            fab_expense_txt.startAnimation(FadOpen);
            fab_expense_txt.setClickable(true);
            isOpen = true;
        }
    }

    public void expenseDataInsert() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        final EditText amount = myView.findViewById(R.id.amount_edt);
        final Spinner type = myView.findViewById(R.id.dropdown2);
        checkBox = myView.findViewById(R.id.category_check);
        editText = myView.findViewById(R.id.edittextCategory);
        type.setVisibility(View.VISIBLE);
        myView.findViewById(R.id.dropdown).setVisibility(View.INVISIBLE);
        final EditText note = myView.findViewById(R.id.note_edt);
        Button btnSave = myView.findViewById(R.id.btnSave);
        Button btnCancel = myView.findViewById(R.id.btnCancel);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.isChecked()) {
                    type.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                } else {
                    type.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmAmount = amount.getText().toString().trim();
                String tmType;
                String tmNote = note.getText().toString().trim();
                if (checkBox.isChecked()) {
                    tmType = editText.getText().toString().trim();
                } else {
                    tmType = type.getSelectedItem().toString().trim();
                }
                if (TextUtils.isEmpty(tmAmount)) {
                    amount.setError("Required field");
                    return;
                }
                int inamount = Integer.parseInt(tmAmount);
                if (TextUtils.isEmpty(tmType)) {
                    Toast.makeText(getActivity(), "Select a type", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(tmNote)) {
                    note.setError("Required field");
                    return;
                }
                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(inamount, tmType, tmNote, id, mDate);
                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data added", Toast.LENGTH_SHORT).show();
                ftAnimation();
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
        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseDataInsert();
            }
        });
    }
}
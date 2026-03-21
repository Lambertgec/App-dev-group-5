package com.group5.gue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.group5.gue.data.user.UserRepository;
import com.group5.gue.data.model.User;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    UserRepository userRepository;
    User user;
    int userScore;

    public CollectFragment() {
        userRepository = UserRepository.Companion.getInstance();
        user = userRepository.getCachedUser();
        userScore = user.getScore();

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CollectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollectFragment newInstance(String param1, String param2) {
        CollectFragment fragment = new CollectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    TextView bocol1;
    TextView bocol2;
    TextView bocol3;
    TextView bocol4;
    TextView bocol5;
    TextView bocol6;
    TextView bocol7;
    TextView bocol8;
    TextView bocol9;
    TextView bocol10;
    TextView bocol11;
    TextView bocol12;
    TextView bocol13;
    TextView bocol14;
    TextView bocol15;

    Button col1;
    Button col2;
    Button col3;
    Button col4;
    Button col5;
    Button col6;
    Button col7;
    Button col8;
    Button col9;
    Button col10;
    Button col11;
    Button col12;
    Button col13;
    Button col14;
    Button col15;

    Button xBut;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    TextView text5;
    TextView text6;
    TextView text7;
    TextView text8;
    TextView text9;
    TextView text10;
    TextView text11;
    TextView text12;

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
        View rv = inflater.inflate(R.layout.fragment_collect, container, false);

        return  rv;    }
    public void setBlackout(View view) {
        bocol1 = view.findViewById(R.id.bocol1);
        bocol2 = view.findViewById(R.id.bocol2);
        bocol3 = view.findViewById(R.id.bocol3);
        bocol4 = view.findViewById(R.id.bocol4);
        bocol5 = view.findViewById(R.id.bocol5);
        bocol6 = view.findViewById(R.id.bocol6);
        bocol7 = view.findViewById(R.id.bocol7);
        bocol8 = view.findViewById(R.id.bocol8);
        bocol9 = view.findViewById(R.id.bocol9);
        bocol10 = view.findViewById(R.id.bocol10);
        bocol11= view.findViewById(R.id.bocol11);
        bocol12 = view.findViewById(R.id.bocol12);
//        bocol13 = view.findViewById(R.id.bocol13);
//        bocol14 = view.findViewById(R.id.bocol14);
//        bocol15 = view.findViewById(R.id.bocol15);

        if (userScore>=100){
            bocol1.setVisibility(view.INVISIBLE);
        }
        if (userScore>=200){
            bocol2.setVisibility(view.INVISIBLE);
        }
        if (userScore>=300){
            bocol3.setVisibility(view.INVISIBLE);
        }
        if (userScore>=400){
            bocol4.setVisibility(view.INVISIBLE);
        }
        if (userScore>=500){
            bocol5.setVisibility(view.INVISIBLE);
        }
        if (userScore>=600){
            bocol6.setVisibility(view.INVISIBLE);
        }
        if (userScore>=700){
            bocol7.setVisibility(view.INVISIBLE);
        }
        if (userScore>=800){
            bocol8.setVisibility(view.INVISIBLE);
        }
        if (userScore>=900){
            bocol9.setVisibility(view.INVISIBLE);
        }
        if (userScore>=1000){
            bocol10.setVisibility(view.INVISIBLE);
        }
        if (userScore>=1100){
            bocol11.setVisibility(view.INVISIBLE);
        }
        if (userScore>=1200){
            bocol12.setVisibility(view.INVISIBLE);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBlackout(view);
        text1 = view.findViewById(R.id.text1);
        text1.setVisibility(view.INVISIBLE);
        text2 = view.findViewById(R.id.text2);
        text2.setVisibility(view.INVISIBLE);
        text3 = view.findViewById(R.id.text3);
        text3.setVisibility(view.INVISIBLE);
        text4 = view.findViewById(R.id.text4);
        text4.setVisibility(view.INVISIBLE);
        text5 = view.findViewById(R.id.text5);
        text5.setVisibility(view.INVISIBLE);
        text6 = view.findViewById(R.id.text6);
        text6.setVisibility(view.INVISIBLE);
        text7 = view.findViewById(R.id.text7);
        text7.setVisibility(view.INVISIBLE);
        text8 = view.findViewById(R.id.text8);
        text8.setVisibility(view.INVISIBLE);
        text9 = view.findViewById(R.id.text9);
        text9.setVisibility(view.INVISIBLE);
        text10 = view.findViewById(R.id.text10);
        text10.setVisibility(view.INVISIBLE);
        text11 = view.findViewById(R.id.text11);
        text11.setVisibility(view.INVISIBLE);
        text12 = view.findViewById(R.id.text12);
        text12.setVisibility(view.INVISIBLE);

        xBut = view.findViewById(R.id.xbut);
        xBut.setVisibility(view.INVISIBLE);

        xBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text1.setVisibility(view.INVISIBLE);
                text2.setVisibility(view.INVISIBLE);
                text3.setVisibility(view.INVISIBLE);
                text4.setVisibility(view.INVISIBLE);
                text5.setVisibility(view.INVISIBLE);
                text6.setVisibility(view.INVISIBLE);
                text7.setVisibility(view.INVISIBLE);
                text8.setVisibility(view.INVISIBLE);
                text9.setVisibility(view.INVISIBLE);
                text10.setVisibility(view.INVISIBLE);
                text11.setVisibility(view.INVISIBLE);
                text12.setVisibility(view.INVISIBLE);
                xBut.setVisibility(view.INVISIBLE);
            }
        });

        col1 = view.findViewById(R.id.col1);

        col1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=100){
                                text1.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        //this part can be added for every collectible, not sure if theres a better way than to copy paste code for all collectibles.

        col2 = view.findViewById(R.id.col2);

        col2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=200){
                                text2.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        //colllectible 3
        col3 = view.findViewById(R.id.col3);

        col3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=300){
                                text3.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col4 = view.findViewById(R.id.col4);

        col4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=400){
                                text4.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col5 = view.findViewById(R.id.col5);

        col5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=500){
                                text5.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col6 = view.findViewById(R.id.col6);

        col6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=600){
                                text6.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col7 = view.findViewById(R.id.col7);

        col7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=700){
                                text7.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col8 = view.findViewById(R.id.col8);

        col8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=800){
                                text8.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col9 = view.findViewById(R.id.col9);

        col9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=900){
                                text9.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col10 = view.findViewById(R.id.col10);

        col10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=1000){
                                text10.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col11 = view.findViewById(R.id.col11);

        col11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=1100){
                                text11.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        col12 = view.findViewById(R.id.col12);

        col12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "You need to be admin for that", Toast.LENGTH_SHORT);
                            toast.show();
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userScore>=1200){
                                text12.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

    }
}